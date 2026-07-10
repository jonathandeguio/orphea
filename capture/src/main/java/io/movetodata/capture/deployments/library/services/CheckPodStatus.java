package io.movetodata.capture.deployments.library.services;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CheckPodStatus {

    public Boolean areUpdatedComponentsPodsRunningSuccessfully(Set<String> updatedComponents) {
        try {
            // Load the Kubernetes config from the default location (~/.kube/config)
            ApiClient client = Config.defaultClient();
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();

            // Get the list of pods in the "boson-dev" namespace
            V1PodList podList = api.listNamespacedPod(System.getenv("KUBERNETES_NAMESPACE"), null, null, null, null, null, null, null, null, null, false);

            // Get the current time
            Instant currentTime = Instant.now();

            // Collect pods related to the updated components that were created in the last 20-30 seconds
            List<V1Pod> recentPods = podList.getItems().stream()
                    .filter(pod -> {
                        String podName = Objects.requireNonNull(pod.getMetadata()).getName();
                        Instant creationTime = Objects.requireNonNull(pod.getMetadata().getCreationTimestamp()).toInstant();
                        Duration durationSinceCreation = Duration.between(creationTime, currentTime);
                        boolean isRecentPod = durationSinceCreation.getSeconds() <= 30;

                        // Check if the pod is related to the updated components
                        boolean isRelatedToUpdatedComponent = updatedComponents.stream().anyMatch(component -> podName.startsWith(component + "-"));
                        return isRelatedToUpdatedComponent && isRecentPod;
                    })
                    .collect(Collectors.toList());

            // Check if all recent pods related to the updated components are in "Running" state
            for (V1Pod pod : recentPods) {
                String podName = Objects.requireNonNull(pod.getMetadata()).getName();
                String podStatus = Objects.requireNonNull(pod.getStatus()).getPhase();

                System.out.println("Pod Name: " + podName + ", Pod Status: " + podStatus + ", Created " + Duration.between(Objects.requireNonNull(pod.getMetadata().getCreationTimestamp()).toInstant(), currentTime).getSeconds() + " seconds ago");

                if (!"Running".equals(podStatus)) {
                    return false; // If any recent pod is not in running state, return false
                }
            }

            return true; // All recent pods related to updated components are in running state

        } catch (IOException | ApiException e) {
            System.out.println("Error checking pod status: " + e);
            return false;
        }
    }
}
