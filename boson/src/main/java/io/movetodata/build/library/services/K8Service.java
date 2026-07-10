package io.movetodata.build.library.services;

import com.esotericsoftware.minlog.Log;
import io.movetodata.build.BobEnums.BuildLanguage;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.library.dto.PreviewSpecsRequest;
import io.movetodata.build.library.keys.PreviewSpecsKey;
import io.movetodata.build.library.models.PreviewSpecsModel;
import io.movetodata.build.library.repository.PreviewSpecsModelRepository;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.keys.HardwareSpecsKey;
import io.movetodata.kitab.library.models.RepositoryHardwareSpecsModel;
import io.movetodata.kitab.library.repository.RepositoryHardwareSpecsRepository;
import io.movetodata.passport.security.TokenProvider;
import io.movetodata.platform.library.models.GitConfigModel;
import io.movetodata.platform.library.models.LicenseInfoModel;
import io.movetodata.platform.library.models.PlatformConfig;
import io.movetodata.platform.library.repository.GitConfigRepository;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.BackFsFileUtils;
import io.movetodata.sharedutils.BackingFS;
import io.movetodata.sharedutils.Exceptions.EnvConfigurationException;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import io.movetodata.sharedutils.KubernetesUtils;
import io.movetodata.sharedutils.SparkUtils;
import io.movetodata.sparkoperator.models.*;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.movetodata.sharedutils.KubernetesUtils.deletePod;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class K8Service {
    private final KitabService kitabService;
    private final PlatformConfigRepository platformConfigRepository;
    private final GitConfigRepository gitConfigRepository;
    private final RepositoryHardwareSpecsRepository repositoryHardwareSpecsRepository;
    private final BackFsFileUtils backFsFileUtils;
    private final PreviewSpecsModelRepository previewSpecsModelRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @NotNull
    public static String getPodName(UUID buildId, BuildType buildType, BuildLanguage buildLanguage, BuildTrigger buildTrigger) {
        String name = "bosler-spark";
        String id = buildId.toString().substring(buildId.toString().lastIndexOf('-') + 1);
        name += "-" + buildLanguage.toString().toLowerCase() + "-" + buildType.toString().toLowerCase() + "-" + buildTrigger.toString().toLowerCase() + "-" + id;
        return name;
    }

    public Map<String, V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs> databaseSecretVaribales(Map<String, V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs> envSecretKeyRefsMap) {

        V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs dbUsername = new V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs();
        dbUsername.setKey("boson-db-username");
        dbUsername.setName("bosler-secret");
        envSecretKeyRefsMap.put("DB_USERNAME", dbUsername);

        V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs dbPassword = new V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs();
        dbPassword.setKey("boson-db-password");
        dbPassword.setName("bosler-secret");
        envSecretKeyRefsMap.put("DB_PASSWORD", dbPassword);

        return envSecretKeyRefsMap;
    }

    public HashMap<String, String> databaseVariables(HashMap<String, String> envVars) {
        envVars.put("DB_HOST", System.getenv("DB_HOST"));
        envVars.put("DATABASE", "kepler"); // TODO : under kubernetes yaml automatic
        return envVars;
    }

    @Transactional
    public RepositoryHardwareSpecsModel getHardwareSpecs(UUID repoId, String branch, String scriptPath, UUID userId) {
        HardwareSpecsKey hardwareSpecsKey = new HardwareSpecsKey(repoId, branch, scriptPath);
        RepositoryHardwareSpecsModel hardwareSpecsModel = new RepositoryHardwareSpecsModel();

        if (!repositoryHardwareSpecsRepository.existsById(hardwareSpecsKey)) {
            hardwareSpecsModel.setRepository(repoId);
            hardwareSpecsModel.setBranch(branch);
            hardwareSpecsModel.setScriptPath(scriptPath);
            hardwareSpecsModel.setCores(1);
            hardwareSpecsModel.setFailureRetries(0);
            hardwareSpecsModel.setMemory("512m");
            hardwareSpecsModel.setNumberOfExecutors(1);

            hardwareSpecsModel.setCreatedAt(new Date());
            hardwareSpecsModel.setCreatedBy(userId);
            hardwareSpecsModel.setUpdatedBy(userId);
            hardwareSpecsModel.setUpdatedAt(new Date());

            repositoryHardwareSpecsRepository.save(hardwareSpecsModel);
        } else {
            hardwareSpecsModel = repositoryHardwareSpecsRepository.findById(hardwareSpecsKey).orElseThrow();
        }

        return hardwareSpecsModel;
    }

    @Transactional
    public void updateHardwareSpecsModel(RepositoryHardwareSpecsModel repositoryHardwareSpecsModel, UUID userId) {
        repositoryHardwareSpecsModel.setUpdatedBy(userId);
        repositoryHardwareSpecsModel.setUpdatedAt(new Date());

        repositoryHardwareSpecsRepository.save(repositoryHardwareSpecsModel);
    }

    public HashMap<String, String> settingEnvironmentVariables(UUID repositoryId, String branch, String scriptPath, String branchId, String commitId, Integer cores, String memory, UUID buildId, UUID userId, BuildType buildType, BuildLanguage buildLanguage, BuildTrigger buildTrigger, String rowLimit, String fileName, String lineNo) {
        PlatformConfig platformConfig = platformConfigRepository.findByName("platformConfig").orElseThrow();
        LicenseInfoModel licenseInfoModel = PlatformConfigService.decrypt(platformConfig.getLicenseKey());

        GitConfigModel gitConfigModel = gitConfigRepository.findByConfig("platform");

        Date expiresIn = new Date(System.currentTimeMillis() + 10 * 60 * 1000000);
        String access_token = tokenProvider.buildToken(userId, expiresIn);

        HashMap<String, String> envVars = new HashMap<>();
        if (repositoryId != null) {
            envVars.put("REPOSITORY_ID", String.valueOf(repositoryId));
        }

        if (branch != null) {
            envVars.put("BRANCH", branch);
        }
        envVars.put("USER_ID", String.valueOf(userId));
        envVars.put("BUILD_TRIGGER", String.valueOf(buildTrigger));
        envVars.put("DEFAULT_BRANCH", platformConfig.getDefaultBranch());
        envVars.put("SOURCE", licenseInfoModel.getBaseUrl());
        envVars.put("TRANSACTION_ID", String.valueOf(new UUID(0, 0)));

        envVars.put("JULIA_URL", "http://" + gitConfigModel.getHost() + ":" + gitConfigModel.getPort().toString() + "/");

        envVars.put("PHYSICAL_ENDPOINT", kitabService.getPhysicalEndpoint());

        if (scriptPath != null) {
            envVars.put("SCRIPT_PATH", scriptPath);
        }

        if (branchId != null) {
            envVars.put("BRANCH_ID", branchId);
        }

        if (commitId != null) {
            envVars.put("COMMIT_ID", commitId);
        }

        if (cores != null) {
            envVars.put("CORES", String.valueOf(cores));
        }

        if (memory != null) {
            envVars.put("MEMORY", memory);
        }

        envVars.put("BUILD_ID", buildId.toString());
        envVars.put("BUILD_TOKEN", access_token);
        envVars.put("BUILD_TYPE", buildType.toString());
        envVars.put("BOSLER_API", "http://boson:8080");

        envVars.put("BOSLER_MOUNT_PATH", System.getenv("BOSLER_MOUNT_PATH"));
        envVars.put("BACKING_FS", System.getenv("BACKING_FS"));

        if (rowLimit != null) {
            envVars.put("ROW_LIMIT", rowLimit);
        }

        if (fileName != null) {
            envVars.put("FILE_NAME", fileName);
        }
        if (lineNo != null) {
            envVars.put("LINE_NO", lineNo);
        }

        if (buildLanguage.equals(BuildLanguage.PYTHON)) { // only send below variables for python transforms
            if (platformConfig.isArtifactory()) {
                envVars.put("ARTIFACTORY_URL", platformConfig.getArtifactoryUrl());
            }

            if (platformConfig.isHttpProxy()) {
                envVars.put("HTTP_PROXY", platformConfig.getHttpProxyUrl());
            }
        }

        return envVars;
    }

    public HashMap<String, String> settingEnvVarsForColumnStats(UUID datasetId, String branch, UUID transactionId, String column, UUID userId, UUID resultsId, String encoding, ResourceSubtype type) {
        HashMap<String, String> envVars = new HashMap<>();
        Date expiresIn = new Date(System.currentTimeMillis() + 10 * 60 * 1000000);
        String access_token = tokenProvider.buildToken(userId, expiresIn);
        envVars.put("ENCODING", encoding);
        envVars.put("PHYSICAL_ENDPOINT", kitabService.getPhysicalEndpoint());
        envVars.put("BRANCH", branch);
        envVars.put("BRANCH_TYPE", type.toString());
        envVars.put("BUILD_TOKEN", access_token);
        envVars.put("BUILD_TYPE", BuildType.DEFAULT.toString());
        envVars.put("DATASET_ID", String.valueOf(datasetId));
        envVars.put("TRANSACTION_ID", String.valueOf(transactionId));
        envVars.put("COLUMN", column);
        envVars.put("RESULTS_ID", String.valueOf(resultsId));
        envVars.put("BUILD_ID", String.valueOf(resultsId));

        envVars.put("BOSLER_MOUNT_PATH", System.getenv("BOSLER_MOUNT_PATH"));
        envVars.put("BACKING_FS", System.getenv("BACKING_FS"));
        envVars.put("BOSLER_API", "http://boson:8080");


        return envVars;
    }

    public HashMap<String, String> settingEnvVarsForDatabaseSync(UUID datasetId, String branch, UUID transactionId, UUID userId, UUID buildId, String encoding, ResourceSubtype type, UUID syncId, boolean createIndexes) {
        HashMap<String, String> envVars = new HashMap<>();
        Date expiresIn = new Date(System.currentTimeMillis() + 10 * 60 * 1000000);
        String access_token = tokenProvider.buildToken(userId, expiresIn);
        envVars.put("ENCODING", encoding);
        envVars.put("PHYSICAL_ENDPOINT", kitabService.getPhysicalEndpoint());
        envVars.put("BRANCH", branch);
        envVars.put("BRANCH_TYPE", type.toString());
        envVars.put("BUILD_TOKEN", access_token);
        envVars.put("BUILD_TYPE", BuildType.DEFAULT.toString());
        envVars.put("DATASET_ID", String.valueOf(datasetId));
        envVars.put("TRANSACTION_ID", String.valueOf(transactionId));
        envVars.put("SYNC_ID", String.valueOf(syncId));
        envVars.put("CREATE_INDEXES", String.valueOf(createIndexes));

        envVars.put("BUILD_ID", String.valueOf(buildId));

        envVars.put("BOSLER_MOUNT_PATH", System.getenv("BOSLER_MOUNT_PATH"));
        envVars.put("BACKING_FS", System.getenv("BACKING_FS"));
        envVars.put("BOSLER_API", "http://boson:8080");

        return envVars;
    }

    public void setupPodSparkSpec(BuildTrigger buildTrigger,
                                  BuildType buildType,
                                  BuildLanguage buildLanguage,
                                  V1beta2SparkApplicationSpec spec,
                                  HashMap<String, String> envVars) {

        if (buildTrigger.equals(BuildTrigger.DATASET)) {
            if (buildLanguage.equals(BuildLanguage.PYTHON)) {
                log.info(">>>> Rebuilding via Dataset in PYTHON <<<<<");
                spec.setType(V1beta2SparkApplicationSpec.TypeEnum.PYTHON);
                spec.setPythonVersion(V1beta2SparkApplicationSpec.PythonVersionEnum._3);
                if (buildType.equals(BuildType.PREVIEW)) {
                    spec.setMainApplicationFile("local:///" + System.getenv("BOSLER_MOUNT_PATH") + "/git/cloned/" + envVars.get("USER_ID") + "/" + envVars.get("REPOSITORY_ID") + "/" + envVars.get("SCRIPT_PATH"));
                } else {
                    spec.setMainApplicationFile("local:///opt/" + envVars.get("REPOSITORY_ID") + "/" + envVars.get("SCRIPT_PATH"));
                }
            } else if (buildLanguage.equals(BuildLanguage.SQL)) {
                log.info(">>>> Rebuilding via Dataset in PYTHON <<<<<");
                spec.setType(V1beta2SparkApplicationSpec.TypeEnum.JAVA);
                spec.setMainApplicationFile("local:///app/boson-0.0.1-SNAPSHOT.jar");
                spec.setMainClass("io.movetodata.build.shyne.SqlTransform");
            }
        } else if (buildTrigger.equals(BuildTrigger.PYTHON)) {
            log.info(">>>> Building in PYTHON <<<<<");
            spec.setType(V1beta2SparkApplicationSpec.TypeEnum.PYTHON);
            spec.setPythonVersion(V1beta2SparkApplicationSpec.PythonVersionEnum._3);
            if (buildType.equals(BuildType.PREVIEW)) {
                spec.setMainApplicationFile("local:///" + System.getenv("BOSLER_MOUNT_PATH") + "/git/cloned/" + envVars.get("USER_ID") + "/" + envVars.get("REPOSITORY_ID") + "/" + envVars.get("SCRIPT_PATH"));
            } else {
                spec.setMainApplicationFile("local:///opt/" + envVars.get("REPOSITORY_ID") + "/" + envVars.get("SCRIPT_PATH"));
            }

        } else if (buildTrigger.equals(BuildTrigger.SQL)) {
            log.info(">>>> Building in SQL <<<<<");
            spec.setType(V1beta2SparkApplicationSpec.TypeEnum.JAVA);
            spec.setMainApplicationFile("local:///app/boson-0.0.1-SNAPSHOT.jar");
            spec.setMainClass("io.movetodata.build.shyne.SqlTransform");

        } else if (buildTrigger.equals(BuildTrigger.COLUMNSTATS)) {
            log.info(">>>> Building in COLUMN STATS <<<<<");
            spec.setType(V1beta2SparkApplicationSpec.TypeEnum.JAVA);
            spec.setMainApplicationFile("local:///app/boson-0.0.1-SNAPSHOT.jar");
            spec.setMainClass("io.movetodata.build.shyne.ColumnStats");

        } else if (buildTrigger.equals(BuildTrigger.SYNCHRO)) {
            log.info(">>>> Building in SYNCHRO <<<<<");
            spec.setType(V1beta2SparkApplicationSpec.TypeEnum.JAVA);
            spec.setMainApplicationFile("local:///app/boson-0.0.1-SNAPSHOT.jar");
            spec.setMainClass("io.movetodata.build.shyne.Synchro");
        } else if (buildTrigger.equals(BuildTrigger.CONNECT)) {
            log.info(">>>> Building in CONNECT <<<<<");
            spec.setType(V1beta2SparkApplicationSpec.TypeEnum.JAVA);
            spec.setMainApplicationFile("local:///app/boson-0.0.1-SNAPSHOT.jar");
            spec.setMainClass("io.movetodata.build.shyne.Synchro");
        } else {
            throw new EnvConfigurationException("Not a valid trigger for creating spec");
        }

    }

    public void podRelatedTasksInTransform(Integer failureRetries,
                                           Integer numberOfExecutors,
                                           Integer cores,
                                           String memory,
                                           UUID buildId,
                                           BuildType buildType,
                                           BuildLanguage buildLanguage,
                                           BuildTrigger buildTrigger,
                                           HashMap<String, String> envVars) throws IOException, ApiException {
        V1beta2SparkApplication sparkApplication = new V1beta2SparkApplication();
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        String namespace = System.getenv("NAMESPACE"); // String | The custom resource's namespace
        v1ObjectMeta.namespace(namespace);
        String name = getPodName(buildId, buildType, buildLanguage, buildTrigger);
        v1ObjectMeta.name(name);

        V1beta2SparkApplicationSpecRestartPolicy restartPolicy = new V1beta2SparkApplicationSpecRestartPolicy();
        restartPolicy.setType(V1beta2SparkApplicationSpecRestartPolicy.TypeEnum.ONFAILURE);
        restartPolicy.setOnFailureRetries(failureRetries);
        restartPolicy.setOnFailureRetryInterval(10L);
        restartPolicy.setOnSubmissionFailureRetries(failureRetries);
        restartPolicy.onSubmissionFailureRetryInterval(20L);

        Map<String, V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs> envSecretKeyRefsMap = new HashMap<>();

        if (Objects.equals(System.getenv("BACKING_FS"), "gs")) {
            V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs googleCloudCredentials = new V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs();
            googleCloudCredentials.setKey("google-cloud-credentials");
            googleCloudCredentials.setName("bosler-secret");

            envSecretKeyRefsMap.put("GOOGLE_CLOUD_CREDENTIALS", googleCloudCredentials);
        }

        if (Objects.equals(System.getenv("BACKING_FS"), "s3")) {
            V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs minioUsername = new V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs();
            minioUsername.setKey("minio-username");
            minioUsername.setName("bosler-secret");

            envSecretKeyRefsMap.put("MINIO_ACCESS_KEY", minioUsername);

            V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs minioPassword = new V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs();
            minioPassword.setKey("minio-password");
            minioPassword.setName("bosler-secret");

            envSecretKeyRefsMap.put("MINIO_SECRET_KEY", minioPassword);


        }

        if (Objects.equals(buildTrigger, BuildTrigger.SYNCHRO)) {
            databaseSecretVaribales(envSecretKeyRefsMap);
            BackingFS.envVarBasedOnBackingFS(envVars);
        }

        Map<String, String> labels = Map.of(
                "version", "3.3.2"
        );

        V1beta2SparkApplicationSpecDriver specDriver = new V1beta2SparkApplicationSpecDriver();
        specDriver.setCores(cores);
        specDriver.setCoreLimit((cores * 1000) + "m");
        specDriver.setCoreRequest((cores * 1000) + "m");
        specDriver.setMemory(memory);
        specDriver.setEnvSecretKeyRefs(envSecretKeyRefsMap);
        specDriver.setEnvVars(envVars);
        specDriver.setLabels(labels);
        specDriver.setServiceAccount("spark");

        V1beta2SparkApplicationSpecExecutor executor = new V1beta2SparkApplicationSpecExecutor();
        executor.setCores(cores);
        executor.setCoreLimit((cores * 1000) + "m");
        executor.setCoreRequest((cores * 1000) + "m");
        executor.setInstances(numberOfExecutors);
        executor.setEnvSecretKeyRefs(envSecretKeyRefsMap);
        executor.setEnvVars(envVars);
        executor.setMemory(memory);
        executor.setLabels(labels);

//        if (Objects.equals(System.getenv("BACKING_FS"), "localfs")) {
            V1beta2SparkApplicationSpecDriverVolumeMounts v1beta2SparkApplicationSpecDriverVolumeMounts = new V1beta2SparkApplicationSpecDriverVolumeMounts();
            v1beta2SparkApplicationSpecDriverVolumeMounts.setMountPath(System.getenv("BOSLER_MOUNT_PATH"));
            v1beta2SparkApplicationSpecDriverVolumeMounts.setName("bosler-local-fs");
            List<V1beta2SparkApplicationSpecDriverVolumeMounts> v1beta2SparkApplicationSpecDriverVolumeMountslist = new ArrayList<>();
            v1beta2SparkApplicationSpecDriverVolumeMountslist.add(v1beta2SparkApplicationSpecDriverVolumeMounts);
            specDriver.setVolumeMounts(v1beta2SparkApplicationSpecDriverVolumeMountslist);
            executor.setVolumeMounts(v1beta2SparkApplicationSpecDriverVolumeMountslist);
//        }

        V1beta2SparkApplicationSpec spec = new V1beta2SparkApplicationSpec();
        setupPodSparkSpec(buildTrigger, buildType, buildLanguage, spec, envVars);

        spec.setMode(V1beta2SparkApplicationSpec.ModeEnum.CLUSTER);
        spec.setImage(System.getenv("BUILD_IMAGE"));

        String imagePullPolicy = System.getenv("IMAGE_PULL_POLICY");
        if (imagePullPolicy == null || imagePullPolicy.equals("IfNotPresent")) {
            imagePullPolicy = "IfNotPresent";
        } else {
            imagePullPolicy = "Always";
        }

        spec.setImagePullPolicy(imagePullPolicy);

        spec.setSparkVersion("3.3.0");
        spec.setRestartPolicy(restartPolicy);


        spec.setDriver(specDriver);
        spec.setExecutor(executor);

//        if (Objects.equals(System.getenv("BACKING_FS"), "localfs")) {
            V1beta2SparkApplicationSpecHostPath v1beta2SparkApplicationSpecHostPath = new V1beta2SparkApplicationSpecHostPath();
            v1beta2SparkApplicationSpecHostPath.setPath(System.getenv("BOSLER_MOUNT_PATH"));

            V1beta2SparkApplicationSpecVolumes v1beta2SparkApplicationSpecVolumes = new V1beta2SparkApplicationSpecVolumes();
            v1beta2SparkApplicationSpecVolumes.setName("bosler-local-fs");
            v1beta2SparkApplicationSpecVolumes.setHostPath(v1beta2SparkApplicationSpecHostPath);

            List<V1beta2SparkApplicationSpecVolumes> v1beta2SparkApplicationSpecVolumesList = new ArrayList<>();
            v1beta2SparkApplicationSpecVolumesList.add(v1beta2SparkApplicationSpecVolumes);

            spec.setVolumes(v1beta2SparkApplicationSpecVolumesList);
//        }


        spec.setSparkConf(SparkUtils.getSparkConf());

        sparkApplication.setApiVersion("sparkoperator.k8s.io/v1beta2");
        sparkApplication.setKind("SparkApplication");
        sparkApplication.setMetadata(v1ObjectMeta);
        sparkApplication.setSpec(spec);

        ApiClient client = KubernetesUtils.kubernetesClient();

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);

        try {
            String group = "sparkoperator.k8s.io"; // String | The custom resource's group name
            String version = "v1beta2"; // String | The custom resource's version
            String plural = "sparkapplications"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            Object body = sparkApplication; // Object | The JSON schema of the Resource to create.
            String pretty = null; // String | If 'true', then the output is pretty printed.
            String dryRun = null; // String | When present, indicates that modifications should not be persisted. An invalid or unrecognized dryRun directive will result in an error response and no further processing of the request. Valid values are: - All: all dry run stages will be processed
            String fieldManager = null; // String | fieldManager is a name associated with the actor or entity that is making these changes. The value must be less than or 128 characters long, and only contain printable characters, as defined by https://golang.org/pkg/unicode/#IsPrint.

            apiInstance.createNamespacedCustomObject(group, version, namespace, plural, body, pretty, dryRun, fieldManager);
        } catch (ApiException e) {
            Log.error("Exception when calling CustomObjectsApi#createNamespacedCustomObject");
            Log.error("Status code: " + e.getCode());
            Log.error("Status message: " + Arrays.toString(e.getStackTrace()));
            Log.error("Status cause: " + e.getCause());
            Log.error("Reason: " + e.getResponseBody());
            Log.error("Response headers: " + e.getResponseHeaders());
            throw e;
        }

    }

    @Async
    public void copyDriverLogAndDeleteFinishedPod(UUID buildId, boolean safeDeletion) throws Exception {
        UUID deletionId = UUID.randomUUID();
        log.info(" POD DELETION ID : {}", deletionId);
        try {

            ApiClient client = KubernetesUtils.kubernetesClient();

            String partialPodIdentifier = buildId.toString().substring(buildId.toString().lastIndexOf('-') + 1) + "-driver";

            CoreV1Api coreApi = new CoreV1Api(client);

            String namespace = System.getenv("NAMESPACE");
            V1Pod pod = null;

            int retryCount = 5;
            int retryIntervalSeconds = 1;

            for (int attempt = 1; attempt <= retryCount; attempt++) {
                List<V1Pod> podList = coreApi
                        .listNamespacedPod(namespace, "false", null, null,
                                null, null, null, null, null,
                                null, null)
                        .getItems();

                for (V1Pod p : podList) {
                    if (Objects.requireNonNull(Objects.requireNonNull(p.getMetadata()).getName()).contains(partialPodIdentifier)) {
                        pod = p;
                        break;
                    }
                }

                if (pod != null) {
                    log.info(">>> Pod found: " + pod.getMetadata().getName());
                    log.info(">>> Initiating deletion in " + safeDeletion + " mode");
                    break;
                }

                log.info(">>> Retrying pod detection");

                if (attempt < retryCount) {
                    Thread.sleep(retryIntervalSeconds * 1000);
                } else {
                    throw new ResourceNotFoundException("Pod not found even after retrying");
                }
            }

            // 5secs to time out
            int timeoutTimeLeft = safeDeletion ? 2000 : 0;

            while (timeoutTimeLeft > 0) {

                // Get the pod status
                V1PodStatus podStatus = pod.getStatus();

                boolean isCompleted =
                        podStatus.getContainerStatuses() != null &&
                                podStatus.getContainerStatuses().stream().allMatch(containerStatus -> containerStatus.getLastState().getTerminated() != null);

                if (isCompleted) {
                    break;
                }

                Thread.sleep(1000);
                timeoutTimeLeft -= 1000;
            }

            byte[] contentBytes = getPodLogs(pod.getMetadata().getName()).getBytes(StandardCharsets.UTF_8);

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "spark-driver.log",
                    "text/plain",
                    new ByteArrayInputStream(contentBytes));

            backFsFileUtils.UploadFile(file, "logs", buildId, "transform", "overwrite", true);
            log.info(">>> POD DELETING.... {}", deletionId);
            deletePod(System.getenv("NAMESPACE"), pod.getMetadata().getName());
            log.info(">>> POD DELETED.... {}", deletionId);

        } catch (Exception e) {
            log.error("Deleting pod failed " + deletionId + " " + e.getMessage() + e.getCause());
        }
    }

    public void handlePodsOnBuildAbort(String podName) throws IOException, ApiException, InterruptedException {
        ApiClient client = KubernetesUtils.kubernetesClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api coreApi = new CoreV1Api(client);

        String namespace = System.getenv("NAMESPACE");

        V1Pod pod = null;
        int retryCount = 5;
        int retryIntervalSeconds = 1;

        for (int attempt = 1; attempt <= retryCount; attempt++) {
            List<V1Pod> podList = coreApi
                    .listNamespacedPod(namespace, "false", null, null,
                            null, null, null, null, null,
                            null, null)
                    .getItems();

            for (V1Pod p : podList) {
                if (Objects.requireNonNull(p.getMetadata()).getName().contains(podName)) {
                    pod = p;
                    break;
                }
            }

            if (pod != null) {
                log.info(">>> Pod deletion in process");
                deletePod(System.getenv("NAMESPACE"), pod.getMetadata().getName());
                log.info(">>> Pod deleted");
                break;
            }
            log.info(">>> Retrying deletion");
            if (attempt < retryCount) {
                Thread.sleep(retryIntervalSeconds * 1000);
            } else {
                throw new ResourceNotFoundException("Pod not found even after retrying");
            }
        }
    }

    public V1Pod getPod(String podName) throws IOException, ApiException {
        ApiClient client = KubernetesUtils.kubernetesClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api coreApi = new CoreV1Api(client);
        String namespace = System.getenv("NAMESPACE");

        List<V1Pod> podList =
                coreApi
                        .listNamespacedPod(namespace, "false", null, null,
                                null, null, null, null, null,
                                null, null)
                        .getItems();


        V1Pod pod = null;
        for (V1Pod p : podList) {
            if (Objects.requireNonNull(p.getMetadata()).getName().contains(podName)) {
                pod = p;
                break;
            }
        }

        if (pod == null) {
            throw new ResourceNotFoundException("Pod not found for " + podName);
        }

        return pod;
    }

    public String getPodLogs(String podName) throws IOException, ApiException {
        ApiClient client = KubernetesUtils.kubernetesClient();
        V1Pod pod = getPod(podName);
        CoreV1Api coreApi = new CoreV1Api(client);
        String namespace = System.getenv("NAMESPACE");
        return coreApi.readNamespacedPodLog(pod.getMetadata().getName(), namespace, null, true, null,
                null, null, null, null, null, null);

    }

    public PreviewSpecsModel getPreviewSpecs(UUID repositoryId, String branch) {
        PreviewSpecsKey previewSpecsKey = new PreviewSpecsKey(repositoryId, branch);
        if (previewSpecsModelRepository.existsById(previewSpecsKey)) {
            return previewSpecsModelRepository.findById(previewSpecsKey).orElseThrow();
        } else {
            PreviewSpecsModel previewSpecsModel = new PreviewSpecsModel();
            previewSpecsModel.setRepositoryId(repositoryId);
            previewSpecsModel.setBranch(branch);
            previewSpecsModel.setRowLimit(100);

            return previewSpecsModelRepository.save(previewSpecsModel);
        }

    }

    public void putPreviewSpecs(UUID repositoryId, String branch, PreviewSpecsRequest previewSpecsRequest) {
        PreviewSpecsKey previewSpecsKey = new PreviewSpecsKey(repositoryId, branch);

        PreviewSpecsModel previewSpecsModel = previewSpecsModelRepository.getReferenceById(previewSpecsKey);
        previewSpecsModel.setRowLimit(previewSpecsRequest.getRowLimit());

        previewSpecsModelRepository.save(previewSpecsModel);
    }
}
