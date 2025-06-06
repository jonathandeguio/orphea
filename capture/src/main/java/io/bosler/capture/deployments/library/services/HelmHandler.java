package io.bosler.capture.deployments.library.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelmHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelmHandler.class);

    private final String helmDir;
    private final String helmChart;
    private final String helmValuesFile;
    private final Map<String, String> componentsToUpdate;

    public HelmHandler(String helmDir, String helmChart, String helmValuesFile, Map<String, String> componentsToUpdate) {
        this.helmDir = helmDir;
        this.helmChart = helmChart;
        this.helmValuesFile = helmValuesFile;
        this.componentsToUpdate = componentsToUpdate;
    }

    public void updateHelmValues() throws Exception {
        String helmValuesPath = Paths.get(helmDir, helmChart, helmValuesFile).toString();
        logger.info("Helm values path: {}", helmValuesPath);

        File helmValues = new File(helmValuesPath);

        if (!helmValues.exists()) {
            throw new Exception(String.format("%s file not found!", helmValuesPath));
        }

        try {
            List<String> lines = Files.readAllLines(helmValues.toPath());
            List<String> updatedLines = lines.stream()
                    .map(line -> {
                        for (Map.Entry<String, String> entry : componentsToUpdate.entrySet()) {
                            String component = entry.getKey();
                            String newVersion = entry.getValue();
                            if (line.contains("  " + component + ":")) {
                                return "  " + component + ": " + newVersion;
                            }
                        }
                        return line;
                    })
                    .collect(Collectors.toList());

            Files.write(helmValues.toPath(), updatedLines);
            logger.info("Updated Helm values for components: {}", componentsToUpdate);
        } catch (IOException e) {
            logger.error("Error updating Helm values file: {}", helmValuesPath, e);
            throw e;
        }
    }

    public void helmUpgrade() {
        try {
            // Retrieve the HELM_PATH from environment variables
            String helmPath = System.getenv("HELM_PATH");
            if (helmPath == null || helmPath.isEmpty()) {
                throw new IllegalStateException("HELM_PATH environment variable is not set or is empty.");
            }

            // Create a ProcessBuilder with the full helm executable path
            ProcessBuilder processBuilder = new ProcessBuilder(
                    helmPath, "upgrade", "bosler", helmChart, "-f", helmChart + "/" + helmValuesFile,
                    "--namespace", "default" // Specify the correct namespace
            )
                    .directory(new File(helmDir))
                    .redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read the output of the command
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }
            }

            // Wait for the process to complete and check the exit code
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Helm upgrade failed with exit code: {}", exitCode);
            } else {
                logger.info("Helm upgrade completed successfully.");
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Helm upgrade process failed", e);
        } catch (IllegalStateException e) {
            logger.error("Helm upgrade failed due to missing HELM_PATH", e);
        }
    }
}
