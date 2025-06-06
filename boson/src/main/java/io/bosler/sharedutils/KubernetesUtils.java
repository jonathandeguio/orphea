package io.bosler.sharedutils;

import io.bosler.build.library.services.K8Service;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
public class KubernetesUtils {
    private final K8Service k8Service;

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
            client.setApiKey(accessToken);

        }
        Configuration.setDefaultApiClient(client);
        return client;
    }

    public static void deletePod(String namespace, String podName) throws ApiException, IOException {
        ApiClient client = kubernetesClient();
        CoreV1Api api = new CoreV1Api(client);

        V1DeleteOptions v1DeleteOptions = new V1DeleteOptions();
        v1DeleteOptions.setGracePeriodSeconds(0L);
        api.deleteNamespacedPod(
                podName,
                namespace,
                null,
                "false",
                0,
                true,
                null,
                v1DeleteOptions);


        // Delete application also
        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        apiInstance.deleteNamespacedCustomObject(
                "sparkoperator.k8s.io",
                "v1beta2",
                namespace,
                "sparkapplications",
                podName,
                null,
                null,
                null,
                null,
                null
        );

    }
}
