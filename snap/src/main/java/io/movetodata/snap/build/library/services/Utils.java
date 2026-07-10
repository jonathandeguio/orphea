package io.movetodata.snap.build.library.services;

import io.movetodata.snap.build.library.enums.BuildStatus;
import io.movetodata.snap.build.library.models.TriggerArtifactModel;
import io.movetodata.snap.build.library.models.TriggerManagerModel;
import io.movetodata.snap.build.library.repository.TriggerArtifactRepository;
import io.movetodata.snap.build.library.repository.TriggerRepository;
import io.movetodata.snap.kitab.library.models.SocketMessage;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class Utils {

    private final TriggerRepository triggerRepository;
    private final TriggerArtifactRepository triggerArtifactRepository;

    @Autowired
    SimpMessagingTemplate template;

    public void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    public String incrementTag(String latestTagName) {
        if (latestTagName == null) {
            return "0.0.1";
        }
        String newTagName;

        // Increment the latest tag or set it to 0.0.1 if no tags found
        String[] parts = latestTagName.split("/");
        String[] versionParts = parts[parts.length - 1].split("\\.");

        int majorNum = Integer.parseInt(versionParts[0]);
        int minorNum = Integer.parseInt(versionParts[1]);
        int patchNum = Integer.parseInt(versionParts[2]);

        if (patchNum < 99) {
            patchNum++;
        } else {
            patchNum = 0;
            if (minorNum < 99) {
                minorNum++;
            } else {
                minorNum = 0;
                majorNum++;
            }
        }

        newTagName = String.format("%d.%d.%d", majorNum, minorNum, patchNum);
        return newTagName;
    }

    public void onError(UUID lastBuildUser, TriggerArtifactModel triggerArtifactModel, Exception e, String message) throws Exception {

        TriggerManagerModel triggerManagerModel = triggerRepository.getReferenceById(triggerArtifactModel.getTriggerManagerModel().getId());
        triggerManagerModel.setBuildStatus(BuildStatus.ERROR);
        triggerManagerModel.setBuildAt(new Date());
        triggerManagerModel.setBuildBy(lastBuildUser);
        triggerRepository.save(triggerManagerModel);

        triggerArtifactModel.setBuildStatus(BuildStatus.ERROR);
        triggerArtifactModel.setFinishedAt(new Date());
        triggerArtifactRepository.save(triggerArtifactModel);

        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage("triggerUpdates");
        template.convertAndSend("/topic/build/triggers", socketMessage);
        socketMessage.setMessage("artifactUpdates");
        template.convertAndSend("/topic/build/artifacts/" + triggerManagerModel.getId(), socketMessage);

        // Read the output of the process
        Path artifactDirectory = createArtifactDirectory(triggerArtifactModel.getId());
        Path logFilePath = artifactDirectory.resolve(triggerArtifactModel.getId() + ".log");

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile()))) {
            // Write the stack trace to the log file
            writer.write(message);
            writer.write("\n");
            writer.write("----------------------------------\n");
            writer.write("\n");
            writer.write(e.getMessage());
        } catch (Exception ex) {
            // Handle any errors that occur while writing to the log file
            ex.printStackTrace();
        }

        Path tempDir = Files.createDirectories(Path.of(System.getenv("ARTIFACT_STORE") + "/cloned-repo/" + triggerArtifactModel.getTriggerManagerModel().getId()));
        cleanUp(String.valueOf(tempDir));

        throw new Exception("[ERROR] : Build not completed");
    }

    public void executeCommand(String command, BufferedWriter logWriter, String logPrefix) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(logPrefix + line);
                logWriter.write(logPrefix + line + "\n");
                logWriter.flush();
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("[ERROR] : Command failed with exit code " + exitCode);
        }
    }
    public void executeCommand(String command, BufferedWriter logWriter, String logPrefix, SimpMessagingTemplate messagingTemplate, UUID triggerArtifactId) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(logPrefix + line);
                logWriter.write(logPrefix + line + "\n");
                logWriter.flush();

                SocketMessage logMessage = new SocketMessage();
                logMessage.setMessage(logPrefix + line);
                messagingTemplate.convertAndSend("/topic/logs/" + triggerArtifactId, logMessage);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("[ERROR] : Command failed with exit code " + exitCode);
        }
    }

    public void cleanUp(String directoryPath) throws IOException {
        System.out.println("[RUNNING] : Cleaning up ");
        deleteDirectory(Path.of(directoryPath));
        System.out.println("[FINISHED] : Cleaning up ");
    }

    private Path createArtifactDirectory(UUID artifactId) throws IOException {
        return Files.createDirectories(Path.of(System.getenv("ARTIFACT_STORE"), "artifacts", artifactId.toString()));
    }
}
