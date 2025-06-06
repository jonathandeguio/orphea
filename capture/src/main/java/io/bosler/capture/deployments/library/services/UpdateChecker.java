package io.bosler.capture.deployments.library.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class UpdateChecker {

    private static final Logger logger = LoggerFactory.getLogger(UpdateChecker.class);

    private final String updateHost;
    private final String deploymentId;
    private final String token;
    private final String helmDir;
    private final String helmChart;
    private final String helmValuesFile;
    private final CheckPodStatus checkPodStatus;
    public void checkUpdates() {
        String helmValuesPath = constructHelmValuesPath();
        Map<String, String> currentVersions = getCurrentVersions(helmValuesPath);
        logger.info("[INFO] : Current versions from Helm file: {}", currentVersions);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(updateHost + "/api/deployments/configuration/check/target/state/" + deploymentId);
            request.addHeader("Authorization", "Bearer " + token);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("API response: {}", EntityUtils.toString(response.getEntity()));
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> targetVersions = mapper.readValue(response.getEntity().getContent(), Map.class);
                List.of("id", "state", "deployedAt").forEach(targetVersions.keySet()::remove);
                logger.info("[INFO] : Target versions after removing keys: {}", targetVersions);

                // Remove "parler" explicitly from targetVersions to avoid upgrading it
                targetVersions.remove("parler");

                // Filter only the required components for comparison
                Set<String> componentsToCompare = Set.of("frontend", "boson", "julia", "callisto", "capture", "boslerDocs", "sparkHistoryServer", "globalVersion");
                Map<String, String> filteredCurrentVersions = new HashMap<>();
                Map<String, String> filteredTargetVersions = new HashMap<>();

                for (String component : componentsToCompare) {
                    if (currentVersions.containsKey(component)) {
                        filteredCurrentVersions.put(component, currentVersions.get(component));
                    }
                    if (targetVersions.containsKey(component)) {
                        filteredTargetVersions.put(component, targetVersions.get(component));
                    }
                }
                // Compare the filtered versions
                if (!filteredCurrentVersions.equals(filteredTargetVersions)) {
                    logger.info("[SUCCESS] : Versions have changed. Updating Helm values and proceeding with upgrade.");

                    // Update Helm values for all changed components
                    HelmHandler helmHandler = new HelmHandler(helmDir, helmChart, helmValuesFile, filteredTargetVersions);
                    helmHandler.updateHelmValues();

                    // Run Helm upgrade only once after all changes are applied
                    helmHandler.helmUpgrade();
                    Boolean podCheckFuture = checkPodStatus.areUpdatedComponentsPodsRunningSuccessfully(filteredTargetVersions.keySet());

                    // Add a callback to handle the result when it's done
                    recursivelyCheckPod(podCheckFuture, filteredTargetVersions);
                } else {
                    logger.info("[INFO] : No changes detected in versions. No updates required.");
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR] : Error while checking updates", e);
        }
    }

    private void recursivelyCheckPod(Boolean podCheckFuture, Map<String, String> filteredTargetVersions) {
        long retryInterval = 20 * 1000; // 20 seconds in milliseconds
        recursivelyCheckPodWithInterval(podCheckFuture, filteredTargetVersions, retryInterval);
    }

    private void recursivelyCheckPodWithInterval(Boolean podCheckFuture, Map<String, String> filteredTargetVersions, long retryInterval) {
            if (podCheckFuture) {
                logger.info("[SUCCESS] : Pods updated successfully.");
                changeCurrentState(filteredTargetVersions);
            } else {
                try {
                    logger.warn("[WARN] Pods not updated successfully. Sleeping for 20 seconds before retrying.");
                    Thread.sleep(retryInterval);
                    // Re-check pod status
                    Boolean newPodCheckFuture = checkPodStatus.areUpdatedComponentsPodsRunningSuccessfully(filteredTargetVersions.keySet());
                    // Recursively check pods without time limit
                    recursivelyCheckPodWithInterval(newPodCheckFuture, filteredTargetVersions, retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during pod status check.", e);
                }
            }
    }

    private String constructHelmValuesPath() {
        return Paths.get(helmDir, helmChart, helmValuesFile).toString();
    }

    private Map<String, String> getCurrentVersions(String helmValuesPath) {
        Map<String, String> versionsMap = new HashMap<>();

        Path helmValuesFile = Paths.get(helmValuesPath);

        try (InputStream inputStream = Files.newInputStream(helmValuesFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);
            logger.info("YAML Data: {}", yamlData);

            if (yamlData != null && yamlData.containsKey("versions")) {
                Map<String, Object> versions = (Map<String, Object>) yamlData.get("versions");

                for (String key : versions.keySet()) {
                    Object versionValue = versions.get(key);
                    if (versionValue != null) {
                        versionsMap.put(key, versionValue.toString());
                    } else {
                        logger.warn("Version for key '{}' is null, skipping...", key);
                        // Alternatively, you can set a default value if appropriate
                        // versionsMap.put(key, "default-value");
                    }
                }
            } else {
                logger.warn("Versions section not found in YAML Data");
            }

        } catch (IOException e) {
            logger.error("Error reading the helm values file: {}", helmValuesFile, e);
        }
        logger.info("Versions Map: {}", versionsMap);
        return versionsMap;
    }

    private void changeCurrentState(Map<String, String> payload) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(updateHost + "/api/deployments/configuration/state/active/" + deploymentId);
            request.addHeader("Authorization", "Bearer " + token);
            request.addHeader("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            StringEntity entity = new StringEntity(mapper.writeValueAsString(payload));
            request.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("API status: {}, response: {}", response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
                } else {
                    logger.info("Successfully updated active state.");
                }
            }
        } catch (IOException e) {
            logger.error("Error while changing current state", e);
        }
    }
}
