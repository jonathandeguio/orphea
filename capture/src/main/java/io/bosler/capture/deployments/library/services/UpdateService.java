package io.bosler.capture.deployments.library.services;

import io.bosler.capture.deployments.library.model.EnvConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);
    private final CheckPodStatus checkPodStatus;

    // Use an AtomicBoolean to track upgrade status
    private static final AtomicBoolean upgradeInProgress = new AtomicBoolean(false);

    public void checkUpdates() {
        // Check if an upgrade is already in progress
        if (!upgradeInProgress.compareAndSet(false, true)) {
            logger.info("An upgrade is already in progress. Skipping this update check.");
            return;
        }

        try {
            // Log the current working directory
            String currentWorkingDir = Paths.get("").toAbsolutePath().toString();
            logger.info("Current working directory: {}", currentWorkingDir);

            String bundleDir = EnvConfig.getBundleDir();
            logger.info("Retrieved BUNDLE_DIR value: {}", bundleDir);

            Path bundleDirPath = Paths.get(bundleDir);

            // Check if bundleDir exists
            if (!Files.exists(bundleDirPath)) {
                logger.error("Bundle directory does not exist: {}", bundleDirPath.toAbsolutePath());
                throw new Exception(String.format("Bundle directory not found: %s", bundleDirPath.toAbsolutePath()));
            }

            // If the bundleDir is found, proceed
            String helmDir = bundleDir + "/deployments/configurations/helm";
            UpdateChecker updateChecker = new UpdateChecker(
                    EnvConfig.getUpdateHost(),
                    EnvConfig.getDeploymentId(),
                    EnvConfig.getToken(),
                    helmDir,
                    EnvConfig.getHelmChart(),
                    EnvConfig.getHelmValues(),
                    checkPodStatus
            );
            updateChecker.checkUpdates();
            logger.info("Update check completed successfully.");

        } catch (Exception e) {
            logger.error("Failed to check updates", e);
        } finally {
            // Reset the flag once the upgrade completes
            upgradeInProgress.set(false);
        }
    }

    // Schedule the checkUpdates to run every 5 minutes
    @Scheduled(fixedRate = 300000)  // 5 minutes = 300000 ms
    public void scheduledCheckUpdates() {
        logger.info("Scheduled check for updates started.");
        checkUpdates();
    }
}