package io.bosler.sharedUtils;

import io.bosler.sparkoperator.models.*;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;

import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class KubernetesUtils {

    public static ApiClient kubernetesClient() throws IOException {
        // Below is connection to kubernetes
        ApiClient client;

        // file path to your KubeConfig
        String kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        File kubeConfigFile = new File(kubeConfigPath);

        if (kubeConfigFile.exists()) {
            client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        } else {
            client = ClientBuilder.cluster().build();
            Path accessTokenFilePath = Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token");
            String accessToken = Files.readString(accessTokenFilePath, StandardCharsets.US_ASCII);
//            client.setAccessToken(accessToken);
            client.setApiKey(accessToken);

//            System.out.println("here is the token : ");
//            System.out.println(accessToken);

        }
        Configuration.setDefaultApiClient(client);
        return client;
    }

    public void sparkWithKubernetes(HashMap<String, String> envVars,
                                    String mainClass,
                                    String trigger,
                                    int cores,
                                    String memory,
                                    int numberOfExecutors,
                                    int failureRetries

    ) throws Exception {

        V1beta2SparkApplication sparkApplication = new V1beta2SparkApplication();

//        UUID buildId = UUID.randomUUID();

        UUID buildId = UUID.fromString(envVars.get("BUILD_ID"));

        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.namespace("bosler");
        v1ObjectMeta.name("bosler-spark-" + trigger + "-" + buildId.toString().substring(buildId.toString().lastIndexOf('-') + 1));

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

        if(Objects.equals(trigger, "synchro")) { // Only send database variables if need to sync with postgres
            databaseSecretVaribales(envSecretKeyRefsMap);
            databaseVaribales(envVars);
        }

        envVars.put("CORES", String.valueOf(cores));
        envVars.put("MEMORY", memory);
        envVars.put("BOSLER_API", "http://boson:8080");

        envVars.put("BOSLER_MOUNT_PATH", System.getenv("BOSLER_MOUNT_PATH"));

        envVars.put("BACKING_FS", System.getenv("BACKING_FS"));

        BackingFS.envVarBasedOnBackingFS(envVars);

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
        spec.setType(V1beta2SparkApplicationSpec.TypeEnum.JAVA);
//        spec.setPythonVersion(V1beta2SparkApplicationSpec.PythonVersionEnum._3);
        spec.setMode(V1beta2SparkApplicationSpec.ModeEnum.CLUSTER);
        spec.setMainApplicationFile("local:///app/shyne-0.0.1-SNAPSHOT.jar");
        spec.setMainClass(mainClass);
        spec.setImage(System.getenv("SYNCHRO_IMAGE"));
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
            String namespace = "bosler"; // String | The custom resource's namespace
            String plural = "sparkapplications"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            Object body = sparkApplication; // Object | The JSON schema of the Resource to create.
            String pretty = null; // String | If 'true', then the output is pretty printed.
            String dryRun = null; // String | When present, indicates that modifications should not be persisted. An invalid or unrecognized dryRun directive will result in an error response and no further processing of the request. Valid values are: - All: all dry run stages will be processed
            String fieldManager = null; // String | fieldManager is a name associated with the actor or entity that is making these changes. The value must be less than or 128 characters long, and only contain printable characters, as defined by https://golang.org/pkg/unicode/#IsPrint.

            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, body, pretty, dryRun, fieldManager);


        } catch (ApiException e) {
            System.err.println("Exception when calling CustomObjectsApi#createNamespacedCustomObject");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Status message: " + Arrays.toString(e.getStackTrace()));
            System.err.println("Status cause: " + e.getCause());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();

//            return new ResponseEntity<>(e.getResponseBody(), HttpStatus.OK);
        }
    }

    public static void deletePod(String namespace, String podName) throws ApiException, IOException {
        ApiClient client = kubernetesClient();
        CoreV1Api api = new CoreV1Api(client);

        V1DeleteOptions options = new V1DeleteOptions();
        options.setGracePeriodSeconds(0L);
        options.setPropagationPolicy("Foreground");

        V1Pod status = api.deleteNamespacedPod(
                podName,
                namespace,
                null,
                null,
                null,
                null,
                null,
                options
        );

        System.out.println("Pod deleted: " + status.toString());
    }

    public Map<String, V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs> databaseSecretVaribales(Map<String, V1beta2SparkApplicationSpecDriverEnvSecretKeyRefs> envSecretKeyRefsMap){

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

    public HashMap<String, String> databaseVaribales(HashMap<String, String> envVars){
        envVars.put("DB_HOST", System.getenv("DB_HOST"));
        envVars.put("DATABASE", "kepler"); // TODO : under kubernetes yaml automatic
        return envVars;
    }
}
