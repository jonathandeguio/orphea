package io.orphea.snap.build.library.services;

import io.orphea.snap.artifact.library.services.TriggerArtifactService;
import io.orphea.snap.build.library.enums.BuildStatus;
import io.orphea.snap.build.library.models.TriggerArtifactModel;
import io.orphea.snap.build.library.models.TriggerManagerModel;
import io.orphea.snap.build.library.repository.TriggerArtifactRepository;
import io.orphea.snap.build.library.repository.TriggerRepository;
import io.orphea.snap.deployments.library.services.DeploymentService;
import io.orphea.snap.kitab.library.models.SocketMessage;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class BuildService {
    @Value("${goHarbor.auth.username}")
    private String GO_HARBOR_USERNAME;
    @Value("${goHarbor.auth.password}")
    private String GO_HARBOR_PASSWORD;
    @Value("${goHarbor.url}")
    private String GO_HARBOR_URL;
    private final TriggerRepository triggerRepository;
    private final TriggerArtifactRepository triggerArtifactRepository;
    private final DeploymentService deploymentService;
    @Autowired
    private TriggerArtifactService triggerArtifactService;
    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private Utils utils;
    @Autowired
    private GitService gitService;

    public TriggerArtifactModel initializeArtifactBuild(TriggerManagerModel model) {

        System.out.println("[STARTING] : Starting build : " + model.getRepoName());
        TriggerManagerModel triggerManagerModel = triggerRepository.findByNameAndBranch(model.getName(), model.getBranch())
                .orElseThrow(() -> new IllegalArgumentException("Trigger not found"));
        triggerManagerModel.setBuildStatus(BuildStatus.ACTIVE);
        triggerRepository.save(triggerManagerModel);

        // Set up the trigger artifact model
        TriggerArtifactModel triggerArtifactModel = new TriggerArtifactModel();
        triggerArtifactModel.setStartedAt(new Date());
        triggerArtifactModel.setBuildStatus(BuildStatus.ACTIVE);
        triggerArtifactModel.setBranch(model.getBranch());
        triggerArtifactModel.setTriggerManagerModel(triggerManagerModel);

        // Send updates for the trigger
        SocketMessage triggerMessage = new SocketMessage();
        triggerMessage.setMessage("triggerUpdates");
        triggerMessage.setBuildStatus(BuildStatus.ACTIVE);  // Include build status
        triggerMessage.setImageName(model.getRepoName());  // Include image name
        triggerMessage.setBranch(model.getBranch());  // Include branch info
        triggerMessage.setTimestamp(new Date());  // Include current timestamp
        template.convertAndSend("/topic/build/triggers", triggerMessage);

        SocketMessage artifactMessage = new SocketMessage();
        artifactMessage.setMessage("artifactUpdates");
        artifactMessage.setArtifactId(triggerArtifactModel.getId());  // Include artifact ID
        artifactMessage.setBuildStatus(BuildStatus.ACTIVE);  // Include build status
        artifactMessage.setTriggerManagerId(triggerManagerModel.getId());  // Include trigger manager ID
        artifactMessage.setTimestamp(new Date());  // Include current timestamp
        template.convertAndSend("/topic/build/artifacts/" + triggerManagerModel.getId(), artifactMessage);

        // Update artifact models
        List<TriggerArtifactModel> triggerArtifactModels = triggerManagerModel.getTriggerArtifactModels() != null
                ? triggerManagerModel.getTriggerArtifactModels()
                : new ArrayList<>();
        triggerArtifactModels.add(triggerArtifactModel);
        triggerManagerModel.setTriggerArtifactModels(triggerArtifactModels);

        TriggerManagerModel savedTriggerManagerModel = triggerRepository.save(triggerManagerModel);

        // Get the ID of the last added trigger artifact model
        List<TriggerArtifactModel> savedTriggerArtifactModels = savedTriggerManagerModel.getTriggerArtifactModels();
        TriggerArtifactModel lastAddedTriggerArtifactModel = savedTriggerArtifactModels.get(savedTriggerArtifactModels.size() - 1);

        return lastAddedTriggerArtifactModel;
    }

    public String buildDockerImage(UUID lastBuildUser, String repositoryPath, String imageName, String imageTag, TriggerArtifactModel triggerArtifactModel, String branch) throws Exception {
        TriggerManagerModel managerModel = triggerArtifactModel.getTriggerManagerModel();
        String projectName = managerModel.getHarborProjectName();
        String configFileName = managerModel.getConfigFileName();
        String logFilePath = System.getenv("LOG_FILE_PATH") + "/artifacts/"+ managerModel.getId() + "/" + branch + "/" + triggerArtifactModel.getId() + ".log"; // Define the path for the build log file
        if (imageTag == null) {
            imageTag = "0.0.1";
        }
        String tagName = GO_HARBOR_URL + "/" + projectName + "/" + imageName + ":" + imageTag;
        String latestTagName = GO_HARBOR_URL + "/" + projectName + "/" + imageName + ":latest";

        File logFile = new File(logFilePath);
        File parentDir = logFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        SocketMessage logMessage = new SocketMessage();
        logMessage.setImageName("artifactLogUpdates");
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFilePath, true))) {

            String normalizedPath = repositoryPath.replace("\\", "/");
            File dockerfile = new File(normalizedPath + "/" + configFileName);

            if (!dockerfile.exists()) {
                throw new FileNotFoundException("Dockerfile not found at: " + dockerfile.getAbsolutePath());
            }

            logWriter.write("[RUNNING] : Starting Docker Build using Buildah\n");
            logMessage.setMessage("[RUNNING] : Starting Docker Build using Buildah\n");
            template.convertAndSend("/topic/logs/" + triggerArtifactModel.getId(), logMessage);

            logWriter.flush();

            String buildCmd = String.format("buildah bud -f %s -t %s %s", dockerfile.getAbsolutePath(), tagName, repositoryPath);
            utils.executeCommand(buildCmd, logWriter, "[BUILD] : ",template,triggerArtifactModel.getId());

            logWriter.write("[RUNNING] : Tagging the image with 'latest'\n");
            logMessage.setMessage("[RUNNING] : Tagging the image with 'latest'\n");
            template.convertAndSend("/topic/logs/" + triggerArtifactModel.getId(), logMessage);

            logWriter.flush();

            String tagCmd = String.format("buildah tag %s %s", tagName, latestTagName);
            utils.executeCommand(tagCmd, logWriter, "[TAG] : ",template,triggerArtifactModel.getId());

            logWriter.write("[RUNNING] : Pushing Docker image to Harbor: " + tagName + "\n");
            logMessage.setMessage("[RUNNING] : Pushing Docker image to Harbor: " + tagName + "\n");
            template.convertAndSend("/topic/logs/" + triggerArtifactModel.getId(), logMessage);
            logWriter.flush();

            String pushCmd = String.format("buildah push --tls-verify=false --creds=%s:%s %s", GO_HARBOR_USERNAME, GO_HARBOR_PASSWORD, tagName);
            utils.executeCommand(pushCmd, logWriter, "[PUSH] : ",template,triggerArtifactModel.getId());

            logWriter.write("[RUNNING] : Pushing Docker image to Harbor with 'latest' tag: " + latestTagName + "\n");
            logMessage.setMessage("[RUNNING] : Pushing Docker image to Harbor with 'latest' tag: " + latestTagName + "\n");
            template.convertAndSend("/topic/logs/" + triggerArtifactModel.getId(), logMessage);

            logWriter.flush();

            String pushLatestCmd = String.format("buildah push --tls-verify=false --creds=%s:%s %s", GO_HARBOR_USERNAME, GO_HARBOR_PASSWORD, latestTagName);
            utils.executeCommand(pushLatestCmd, logWriter, "[PUSH-LATEST] : ",template,triggerArtifactModel.getId());

            System.out.println("[SUCCESS] : Image built, tagged, and pushed successfully.\n");
            logWriter.write("[SUCCESS] : Image built, tagged, and pushed successfully.\n");
            logMessage.setMessage("[SUCCESS] : Image built, tagged, and pushed successfully.\n");
            template.convertAndSend("/topic/logs/" + triggerArtifactModel.getId(), logMessage);
            logWriter.flush();

        } catch (Exception e) {
            e.printStackTrace();
            try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFilePath, true))) {
                logWriter.write("[ERROR] : Build, tag, or push not completed: " + e.getMessage() + "\n");
                logMessage.setMessage("[ERROR] : Build, tag, or push not completed: " + e.getMessage() + "\n");
                template.convertAndSend("/topic/logs/" + triggerArtifactModel.getId(), logMessage);

            }
            utils.onError(lastBuildUser, triggerArtifactModel, e, "[ERROR] : Build, tag, or push not completed");
            throw e;
        }

        return "Success";
    }

    public void buildAndSaveDockerImage(UUID lastBuildUser, Git git, TriggerArtifactModel artifactModel, String directoryPath, TriggerManagerModel triggerManagerModel, String imageTag) throws Exception {
        TriggerArtifactModel triggerArtifactModel = updateBuildStatus(artifactModel);
        try {
            String imageId = buildDockerImage(lastBuildUser, directoryPath, triggerManagerModel.getRepoName(), imageTag, triggerArtifactModel, triggerManagerModel.getBranch());
        } catch (Exception e) {
            utils.onError(lastBuildUser, triggerArtifactModel, e, "[ERROR] : Build not completed");
        }

        updateTriggerArtifactModel(lastBuildUser, git, triggerArtifactModel, imageTag);

        utils.cleanUp(directoryPath);
    }

    public TriggerArtifactModel updateBuildStatus(TriggerArtifactModel model) {
        model.setBuildStatus(BuildStatus.ACTIVE);
        return triggerArtifactRepository.save(model);
    }

    public void updateTriggerArtifactModel(UUID lastBuildUser, Git git, TriggerArtifactModel triggerArtifactModel, String imageTag) throws Exception {
        // Get the latest commit ID
        String commitId = gitService.getLatestCommitId(git);
        TriggerManagerModel triggerManagerModel = triggerRepository.getReferenceById(triggerArtifactModel.getTriggerManagerModel().getId());
        triggerManagerModel.setBuildStatus(BuildStatus.SUCCESS);
        triggerManagerModel.setBuildAt(new Date());
        triggerManagerModel.setBuildBy(lastBuildUser);
        triggerManagerModel.setCommitId(commitId);
        triggerManagerModel.setLatestTag(imageTag);
        triggerRepository.save(triggerManagerModel);

        triggerArtifactModel.setBuildStatus(BuildStatus.SUCCESS);
        triggerArtifactModel.setTag(imageTag);
        triggerArtifactModel.setCommitId(commitId);
        triggerArtifactModel.setFinishedAt(new Date());
        triggerArtifactRepository.save(triggerArtifactModel);

        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage("triggerUpdates");
        template.convertAndSend("/topic/build/triggers", socketMessage);
        socketMessage.setMessage("artifactUpdates");
        template.convertAndSend("/topic/build/artifacts/" + triggerManagerModel.getId(), socketMessage);
        if (triggerManagerModel.getName().endsWith(triggerManagerModel.getBranch())) {
            deploymentService.updateAutomaticDeployments(triggerArtifactModel.getTriggerManagerModel().getRepoName(), imageTag, triggerArtifactModel.getBranch());
        }
        triggerArtifactService.deleteOldArtifacts(triggerArtifactModel.getTriggerManagerModel().getName(), 5, triggerArtifactModel.getBranch());
    }
}
