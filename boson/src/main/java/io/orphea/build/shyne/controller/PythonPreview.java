package io.orphea.build.shyne.controller;

import io.orphea.build.BobEnums.BuildType;
import io.orphea.build.library.exception.FunnelServiceException;
import io.orphea.build.library.services.FunnelUtils;
import io.orphea.build.shyne.Utils.ShyneLogging;
import io.orphea.build.shyne.Utils.Utils;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PythonPreview {

    private final FunnelUtils funnelUtils;

    ShyneLogging logger = new ShyneLogging();

    public void pythonTransform(Map<String, String> envVars, BuildType buildType, UUID userId) {

        try {

            String repositoryPath = System.getenv("BOSLER_MOUNT_PATH") + "/tmp/BoslerTransformTemporary/" + userId + "/" + envVars.get("REPOSITORY_ID");
            String repositoryId = envVars.get("REPOSITORY_ID");

            Utils.cloneShyneRepo(repositoryPath, repositoryId);

            try {
                String pythonScriptPath = repositoryPath + "/" + envVars.get("SCRIPT_PATH");

                ProcessBuilder processBuilder = new ProcessBuilder("python3", pythonScriptPath);
                processBuilder.redirectErrorStream(true);


                // Get the environment variables map from the process builder
                Map<String, String> environment = processBuilder.environment();

                // Set the environment variables from the HashMap
                for (Map.Entry<String, String> entry : envVars.entrySet()) {
//                    System.out.println(entry.getKey());
                    if (entry.getKey() != null && entry.getValue() != null)
                        environment.put(entry.getKey(), entry.getValue());
                }

                Process process = processBuilder.start();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                }
                String result = builder.toString();

                System.out.println(result);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            throw new FunnelServiceException("Build failed with error " + e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }


}
