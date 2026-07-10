package io.movetodata.bob.library.services;

import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.bob.library.models.BuildLog;
import io.movetodata.bob.library.models.BuildSpecification;
import io.movetodata.bob.library.repository.BuildLogRepository;
import io.movetodata.bob.library.repository.BuildSpecificationsRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.JwtService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.scheduler.library.repository.SchedulerRepository;
import io.movetodata.scheduler.library.services.SchedulerService;
import io.movetodata.sharedUtils.KubernetesUtils;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.movetodata.sharedUtils.SparkUtils;
import io.movetodata.sparkoperator.models.*;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class BuildService {


    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final SchedulerService schedulerService;
    private final PipelineRepository pipelineRepository;
    private final SchedulerRepository schedulerRepository;
    private final OkResponse response = new OkResponse();

    private final BuildLogRepository buildLogRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;


    public ResponseEntity<Object> buildPythonTransform(UUID userId,
                                                       UUID repositoryId,
                                                       String branch,
                                                       String scriptPath,
                                                       String branchId,
                                                       String commitId,
                                                       int cores,
                                                       String memory,
                                                       int numberOfExecutors,
                                                       int failureRetries,
                                                       String trigger

    ) throws Exception {

        V1beta2SparkApplication sparkApplication = new V1beta2SparkApplication();

        UUID buildId = UUID.randomUUID();
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.namespace("movetodata");
        v1ObjectMeta.name("movetodata-spark-build-" + buildId.toString().substring(buildId.toString().lastIndexOf('-') + 1));

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
            googleCloudCredentials.setName("movetodata-secret");

            envSecretKeyRefsMap.put("GOOGLE_CLOUD_CREDENTIALS", googleCloudCredentials);

        }

        V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs fractalTemplatesToken = new V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs();
        fractalTemplatesToken.setKey("fractal-templates-token");
        fractalTemplatesToken.setName("movetodata-secret");

        envSecretKeyRefsMap.put("FRACTAL_TEMPLATES_TOKEN", fractalTemplatesToken);

        Date expiresIn = new Date(System.currentTimeMillis() + 10 * 60 * 1000000);


        var user = userRepository.findById(userId)
                .orElseThrow();
        var access_token = jwtService.generateToken(user);

        HashMap<String, String> envVars = new HashMap<>();
        envVars.put("REPOSITORY", String.valueOf(repositoryId));
        envVars.put("BRANCH", branch);
        envVars.put("JULIA_URL", "http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/");

        envVars.put("SCRIPT_PATH", scriptPath);
        envVars.put("BRANCH_ID", branchId);
        envVars.put("COMMIT_ID", commitId);
        envVars.put("CORES", String.valueOf(cores));
        envVars.put("MEMORY", memory);
        envVars.put("BUILD_ID", buildId.toString());
        envVars.put("BUILD_TOKEN", access_token);
        envVars.put("MOVETODATA_API", "http://boson:8080");


        Map<String, String> labels = Map.of(
                "version", "3.2.0"
        );

        V1beta2SparkApplicationSpecDriver specDriver = new V1beta2SparkApplicationSpecDriver();
        specDriver.setCores(cores);
//        specDriver.setCoreLimit("1200m");
        specDriver.setMemory(memory);
        specDriver.setEnvSecretKeyRefs(envSecretKeyRefsMap);
        specDriver.setEnvVars(envVars);
        specDriver.setLabels(labels);
        specDriver.setServiceAccount("spark");

        V1beta2SparkApplicationSpecExecutor executor = new V1beta2SparkApplicationSpecExecutor();
        executor.setCores(cores);
        executor.setInstances(numberOfExecutors);
//        executor.setCoreLimit("1200m");
        executor.setEnvSecretKeyRefs(envSecretKeyRefsMap);
        executor.setEnvVars(envVars);
        executor.setMemory(memory);
        executor.setLabels(labels);

        V1beta2SparkApplicationSpec spec = new V1beta2SparkApplicationSpec();
        spec.setType(V1beta2SparkApplicationSpec.TypeEnum.PYTHON);
        spec.setPythonVersion(V1beta2SparkApplicationSpec.PythonVersionEnum._3);
        spec.setMode(V1beta2SparkApplicationSpec.ModeEnum.CLUSTER);
        spec.setMainApplicationFile("local:///opt/" + repositoryId + "/" + scriptPath);
        spec.setImage(System.getenv("SPARK_PYSPARK_IMAGE"));
//        spec.setImagePullPolicy("IfNotPresent");
        spec.setImagePullPolicy("Always");
        spec.setSparkVersion("3.3.0");
        spec.setRestartPolicy(restartPolicy);
        spec.setDriver(specDriver);
        spec.setExecutor(executor);

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
            String namespace = "movetodata"; // String | The custom resource's namespace
            String plural = "sparkapplications"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            Object body = sparkApplication; // Object | The JSON schema of the Resource to create.
            String pretty = null; // String | If 'true', then the output is pretty printed.
            String dryRun = null; // String | When present, indicates that modifications should not be persisted. An invalid or unrecognized dryRun directive will result in an error response and no further processing of the request. Valid values are: - All: all dry run stages will be processed
            String fieldManager = null; // String | fieldManager is a name associated with the actor or entity that is making these changes. The value must be less than or 128 characters long, and only contain printable characters, as defined by https://golang.org/pkg/unicode/#IsPrint.

            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, body, pretty, dryRun, fieldManager);

//            System.out.println("BUILD SERVICE DEBUG");
//            System.out.println(result);
//            System.out.println("BUILD SERVICE DEBUG");

            // Mark status in the build log
            BuildLog buildLog = new BuildLog();

            buildLog.setId(buildId);

            buildLog.setStatus("active");
            buildLog.setTrigger(trigger + "-python");
            buildLog.setStartedAt(new Date());
            buildLog.setStartedBy(userId);

            BuildLog buildLogSaved = buildLogRepository.save(buildLog);


//            buildLog.setRepository(repositoryId);
//            buildLog.setBranch(branch);
//            buildLog.setBranchId(branchId);
//            buildLog.setCommitId(commitId);
//            buildLog.setTrigger(trigger);
//            buildLog.setScriptPath(scriptPath);

            return new ResponseEntity<>(buildLogSaved, HttpStatus.OK);

        } catch (ApiException e) {
            System.err.println("Exception when calling CustomObjectsApi#createNamespacedCustomObject");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Status message: " + Arrays.toString(e.getStackTrace()));
            System.err.println("Status cause: " + e.getCause());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();

            return new ResponseEntity<>(e.getResponseBody(), HttpStatus.OK);
        }
    }

    public void buildBySource(UUID userId, BuildSpecification buildSpecificationRequest) throws Exception {


        List<PipelineModel.TargetDatasetAndTargetBranch> targetDatasets = pipelineRepository.findAllBySourceDatasetAndSourceBranch(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());

        if (!targetDatasets.isEmpty()) {

            for (PipelineModel.TargetDatasetAndTargetBranch targetDataset : targetDatasets) {

                boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(
                        targetDataset.getTargetDataset(),
                        targetDataset.getTargetBranch(),
                        buildSpecificationRequest.getRepository(),
                        buildSpecificationRequest.getScriptPath(),
                        buildSpecificationRequest.getLanguage());

                if (!buildSpecificationExists) {  // Skipping build, same script found. no point building it.

                    boolean schedulerJobInfo = schedulerRepository.existsByDatasetIdAndBranchAndTriggerType(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch(), "bySource");

                    if (schedulerJobInfo) { // Only build if it is bySource, if it is under timed schedule then ignore

                        BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());

                        if (buildSpecification.getId() != null) {
                            UUID repository = buildSpecification.getRepository();
                            String branch = buildSpecification.getBranch();
                            String scriptPath = buildSpecification.getScriptPath();
                            String branchId = buildSpecification.getBranchId();
                            String commitId = buildSpecification.getCommitId();
                            int cores = buildSpecification.getCores();
                            String memory = buildSpecification.getMemory();
                            int numberOfExecutors = buildSpecification.getNumberOfExecutors();
                            int failureRetries = buildSpecification.getFailureRetries();
                            String trigger = "scheduleBySource";

                            buildPythonTransform(userId,
                                    repository,
                                    branch,
                                    scriptPath,
                                    branchId,
                                    commitId,
                                    cores,
                                    memory,
                                    numberOfExecutors,
                                    failureRetries,
                                    trigger);

                        }
                    }

//                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------");
//                    System.out.println(buildSpecificationRequest);
//                    System.out.println(targetDatasets);
//                    System.out.println(targetDataset.getTargetDataset());
//                    System.out.println(targetDataset.getTargetBranch());
//                    System.out.println(schedulerRepository.findAllByDatasetIdAndBranchAndTriggerType(targetDataset.getTargetDataset(), targetDataset.getTargetBranch(), "bySource").isEmpty());
//                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------");


                }

//                else {
//                    return new ResponseEntity<>("Skipping build, same script found. no point building it.", HttpStatus.OK);
//                }
            }
//            return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);
        }

    }

    public void checkAndBuildBySource(UUID datasetId, String sourceBranch, UUID userId) throws Exception {

        List<PipelineModel.TargetDatasetAndTargetBranch> targetDatasets = pipelineRepository.findAllBySourceDatasetAndSourceBranch(datasetId, sourceBranch);

        if (!targetDatasets.isEmpty()) {

            for (PipelineModel.TargetDatasetAndTargetBranch targetDataset : targetDatasets) {

//                boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(
//                        targetDataset.getTargetDataset(),
//                        targetDataset.getTargetBranch(),
//                        buildSpecificationRequest.getRepository(),
//                        buildSpecificationRequest.getScriptPath(),
//                        buildSpecificationRequest.getLanguage());

//                if (!buildSpecificationExists) {  // Skipping build, same script found. no point building it.

                boolean schedulerJobInfo = schedulerRepository.existsByDatasetIdAndBranchAndTriggerType(targetDataset.getTargetDataset(), targetDataset.getTargetBranch(), "bySource");

                if (schedulerJobInfo) { // Only build if it is bySource, if it is under timed schedule then ignore

                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(targetDataset.getTargetDataset(), targetDataset.getTargetBranch());

                    if (buildSpecification.getId() != null) {
                        UUID repository = buildSpecification.getRepository();
                        String branch = buildSpecification.getBranch();
                        String scriptPath = buildSpecification.getScriptPath();
                        String branchId = buildSpecification.getBranchId();
                        String commitId = buildSpecification.getCommitId();
                        int cores = buildSpecification.getCores();
                        String memory = buildSpecification.getMemory();
                        int numberOfExecutors = buildSpecification.getNumberOfExecutors();
                        int failureRetries = buildSpecification.getFailureRetries();
                        String trigger = "scheduleBySource";

                        buildPythonTransform(userId,
                                repository,
                                branch,
                                scriptPath,
                                branchId,
                                commitId,
                                cores,
                                memory,
                                numberOfExecutors,
                                failureRetries,
                                trigger);

                    }
                }

//                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------");
//                    System.out.println(buildSpecificationRequest);
//                    System.out.println(targetDatasets);
//                    System.out.println(targetDataset.getTargetDataset());
//                    System.out.println(targetDataset.getTargetBranch());
//                    System.out.println(schedulerRepository.findAllByDatasetIdAndBranchAndTriggerType(targetDataset.getTargetDataset(), targetDataset.getTargetBranch(), "bySource").isEmpty());
//                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------");


//                }

//                else {
//                    return new ResponseEntity<>("Skipping build, same script found. no point building it.", HttpStatus.OK);
//                }
            }
//            return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);
        }

    }

    public ResponseEntity<Object> buildSqlTransform(UUID userId,
                                  UUID repositoryId,
                                  String branch,
                                  String scriptPath,
                                  String branchId,
                                  String commitId,
                                  int cores,
                                  String memory,
                                  int numberOfExecutors,
                                  int failureRetries,
                                  String trigger) throws Exception {


        UUID buildId = UUID.randomUUID();

        HashMap<String, String> envVars = new HashMap<>();
        envVars.put("BUILD_ID", String.valueOf(buildId));
        envVars.put("REPOSITORY_ID", String.valueOf(repositoryId));
        envVars.put("BRANCH", branch);
        envVars.put("SCRIPT_PATH", scriptPath);
        envVars.put("BRANCH_ID", branchId);
        envVars.put("COMMIT_ID", commitId);

        envVars.put("JULIA_HOST", System.getenv("JULIA_HOST"));
        envVars.put("JULIA_PORT", System.getenv("JULIA_PORT"));



        long currentTimeMillis = System.currentTimeMillis(); // Get current time in milliseconds
        long millisInOneDay = 12 * 60 * 60 * 1000L; // Calculate milliseconds in 12 hours
        Date expiresIn = new Date(currentTimeMillis + millisInOneDay); // Add one day to the current time

        var user = userRepository.findById(userId)
                .orElseThrow();
        var access_token = jwtService.generateToken(user);

        envVars.put("ACCESS_TOKEN", access_token);

        new KubernetesUtils().sparkWithKubernetes(envVars, "io.movetodata.SqlTransform", trigger + "-sql", cores, memory, numberOfExecutors, failureRetries);

        BuildLog buildLog = new BuildLog();

        buildLog.setId(buildId);

        buildLog.setStatus("active");
        buildLog.setTrigger(trigger);
        buildLog.setStartedAt(new Date());
        buildLog.setStartedBy(userId);

        BuildLog buildLogSaved = buildLogRepository.save(buildLog);

        return new ResponseEntity<>(buildLogSaved, HttpStatus.OK);

    }

}
