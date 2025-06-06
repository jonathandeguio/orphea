package io.bosler.snap.artifact.controllers;

import io.bosler.snap.artifact.library.models.CheckUpdatesModel;
import io.bosler.snap.build.library.enums.BuildStatus;
import io.bosler.snap.build.library.models.TriggerArtifactModel;
import io.bosler.snap.build.library.models.TriggerManagerModel;
import io.bosler.snap.build.library.repository.TriggerArtifactRepository;
import io.bosler.snap.build.library.repository.TriggerRepository;
import io.bosler.snap.passport.library.service.UserService;
import io.bosler.snap.artifact.library.repository.ArtifactRepository;
import io.bosler.snap.artifact.library.services.ArtifactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/artifact")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Artifacts", description = "Artifacts Management")
public class ArtifactController {
    private final UserService userService;
    private final ArtifactRepository artifactRepository;
    private final ArtifactService artifactService;
    private final TriggerRepository triggerRepository;
    private final TriggerArtifactRepository triggerArtifactRepository;


    @Operation(summary = "Get available Artifacts by Trigger Id")
    @GetMapping("/byTrigger/{Id}")
    public ResponseEntity<Object> byTrigger(Principal principal, @PathVariable("Id") UUID triggerId) {
        UUID userId = userService.getUser(principal.getName()).getId();

        Optional<TriggerManagerModel> triggerManagerModel = triggerRepository.findByIdOrderByTriggerArtifactModelsStartedAtDesc(triggerId);

        List<TriggerArtifactModel> triggerArtifactModels = triggerArtifactRepository.findByTriggerManagerModelIdOrderByStartedAtDesc(triggerManagerModel.get().getId());

        return new ResponseEntity<>(triggerArtifactModels, HttpStatus.OK);
    }

    @Operation(summary = "Get available Artifact by Trigger Name")
    @GetMapping("/byTriggerName/{repoName}/{branch}")
    public ResponseEntity<Object> byTriggerName(Principal principal, @PathVariable("repoName") String repoName, @PathVariable("branch") String branch) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if ("boslerDocs".equals(repoName)) {
            repoName = "bosler-docs";
        } else if ("sparkHistoryServer".equals(repoName)) {
            repoName = "spark-history-server";
        }

        Optional<TriggerManagerModel> triggerManagerModel = triggerRepository.findByNameAndBranch(repoName + "-" + branch, branch);

        return new ResponseEntity<>(triggerManagerModel, HttpStatus.OK);
    }

    @Operation(summary = "Get Artifacts Log by Artifact Id")
    @GetMapping("/log/{Id}")
    public ResponseEntity<Object> logByTrigger(Principal principal, HttpServletResponse response, @PathVariable("Id") UUID triggerId) throws IOException {
        UUID userId = userService.getUser(principal.getName()).getId();

        Optional<TriggerArtifactModel> triggerArtifactModel = triggerArtifactRepository.findById(triggerId);
//        String logFilePath = System.getenv("DOCKER_BUILD_LOG_STORE") + artifactId + "-" + branch + "/" + imageName + ":" + imageTag + ".log";
        String artifactLogPath = System.getenv("LOG_FILE_PATH") + "/artifacts/" + triggerArtifactModel.get().getTriggerManagerModel().getId() + "/" + triggerArtifactModel.get().getBranch() + "/" + triggerId + ".log";
        System.out.println("LogFilePath : " + artifactLogPath);

        File file = new File(artifactLogPath);

        if (!file.exists()) {
            return new ResponseEntity<>("No detailed logs found", HttpStatus.NO_CONTENT);
        }

        FileSystemResource resource = new FileSystemResource(artifactLogPath);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + triggerId + ".log");
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
        response.flushBuffer();


        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @Operation(summary = "Check Updates")
    @PostMapping("/checkUpdates")
    public ResponseEntity<?> checkUpdates(Principal principal, @RequestBody CheckUpdatesModel checkUpdatesModel) {
        Map<String, Object> response = new HashMap<>();
        boolean newUpdates = false;
        CheckUpdatesModel checkUpdatesModelUpdated = new CheckUpdatesModel();

        // List of component names
        List<String> componentNames = Arrays.asList(
                "boson", "frontend", "parler",
                "julia", "callisto", "callisto", "bosler-docs", "spark-history-server",
                "playground"
        );

        // Iterate over each component
        for (String componentName : componentNames) {
            // Find the trigger manager model by component name
            TriggerManagerModel trigger = triggerRepository.findByName(componentName)
                    .orElseThrow(() -> new IllegalArgumentException("Trigger not found"));

            // Check if the latest tag for the component is different from the one provided
            if (trigger != null) {
                if (trigger.getBuildStatus() == BuildStatus.SUCCESS) {
                    String latestTag = trigger.getLatestTag();
                    String currentTag = artifactService.getTagForComponent(checkUpdatesModel, componentName);
                    if (!Objects.equals(latestTag, currentTag)) {
                        // Update the corresponding field in the updated model if the latest tag is not null
                        if (latestTag != null) {
                            System.out.printf("latestTag " + latestTag);
                            artifactService.setTagForComponent(checkUpdatesModelUpdated, componentName, latestTag);
                            newUpdates = true;
                        }
                    }
                }
            }
        }

        response.put("newUpdates", newUpdates);
        response.put("versions", checkUpdatesModelUpdated);

        return ResponseEntity.ok().body(response);
    }


    @Operation(summary = "Download Artifact Image")
    @GetMapping("/downloadByTriggerId/{triggerId}/{tagName}")
    public ResponseEntity<?> downloadFile(@PathVariable UUID triggerId, @PathVariable String tagName) {
        // Get trigger object using triggerId
        if (!triggerRepository.existsById(triggerId)) {
            return new ResponseEntity<>("No Trigger with the id exist", HttpStatus.NOT_FOUND);
        }

        List<TriggerArtifactModel> newTriggerArtifactModel = triggerArtifactRepository.findByTagAndBuildStatus(tagName, BuildStatus.SUCCESS);

        TriggerArtifactModel newTriggerArtifactModel1 = null;
        for (TriggerArtifactModel triggerArtifactModel : newTriggerArtifactModel) {
            UUID trigger_id = triggerArtifactModel.getTriggerManagerModel().getId();

            if (trigger_id == triggerId) {
                newTriggerArtifactModel1 = triggerArtifactModel;
            }
        }

        if (newTriggerArtifactModel1 != null) {
            UUID artifactId = newTriggerArtifactModel1.getId();

            Path artifacts = Path.of(System.getenv("ARTIFACT_STORE"), "artifacts", artifactId.toString());
            Path artifactPath = artifacts.resolve(artifactId + ".tar").normalize();

            try {
                // Load file as a resource
                Resource resource = new UrlResource(artifactPath.toUri());

                // Check if the file exists
                if (!resource.exists()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }

                // Set content disposition header to prompt download
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

                // Return ResponseEntity with file content and headers
                return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return new ResponseEntity<>("Artifact not found.", HttpStatus.OK);
        }
    }

    @Operation(summary = "Download Latest Image by trigger name")
    @GetMapping("/downloadByTriggerName/{triggerName}")
    public ResponseEntity<?> downloadFileByTriggerName(@PathVariable String triggerName) {
        // Get trigger object using triggerId

        if ("boslerDocs".equals(triggerName)) {
            triggerName = "bosler-docs";
        } else if ("sparkHistoryServer".equals(triggerName)) {
            triggerName = "spark-history-server";
        }

        TriggerManagerModel triggerManagerModel = triggerRepository.findByName(triggerName)
                .orElseThrow(() -> new IllegalArgumentException("Trigger not found"));

        if (triggerManagerModel == null) {
            return new ResponseEntity<>("No Trigger with the name exist", HttpStatus.NOT_FOUND);
        }

        List<TriggerArtifactModel> newTriggerArtifactModel = triggerArtifactRepository.findByTagAndBuildStatus(triggerManagerModel.getLatestTag(), BuildStatus.SUCCESS);

        TriggerArtifactModel newTriggerArtifactModel1 = null;
        for (TriggerArtifactModel triggerArtifactModel : newTriggerArtifactModel) {
            UUID trigger_id = triggerArtifactModel.getTriggerManagerModel().getId();

            if (trigger_id == triggerManagerModel.getId()) {
                newTriggerArtifactModel1 = triggerArtifactModel;
            }
        }

        if (newTriggerArtifactModel1 != null) {
            UUID artifactId = newTriggerArtifactModel1.getId();

            Path artifacts = Path.of(System.getenv("ARTIFACT_STORE"), "artifacts", artifactId.toString());
            Path artifactPath = artifacts.resolve(artifactId + ".tar").normalize();

            try {
                // Load file as a resource
                Resource resource = new UrlResource(artifactPath.toUri());

                // Check if the file exists
                if (!resource.exists()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }

                // Set content disposition header to prompt download
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

                // Return ResponseEntity with file content and headers
                return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return new ResponseEntity<>("Artifact not found.", HttpStatus.OK);
        }
    }

    @Operation(summary = "List Artifacts by trigger name")
    @GetMapping("/listByTriggerName/{repoName}/{branch}")
    public ResponseEntity<?> listArtifactsByTriggerName(@PathVariable("repoName") String repoName, @PathVariable("branch") String branch) {
        // Get trigger object using triggerName
        Optional<TriggerManagerModel> triggerManagerModelOptional = triggerRepository.findByRepoNameAndBranch(repoName, branch);

        if (triggerManagerModelOptional.isEmpty()) {
            return new ResponseEntity<>("No Trigger with the name exist", HttpStatus.NOT_FOUND);
        }
        List<String> tags = new ArrayList<>();
        TriggerManagerModel triggerManagerModel = triggerManagerModelOptional.get();
        for (TriggerArtifactModel triggerArtifactModel : triggerManagerModel.getTriggerArtifactModels()) {
//            TriggerArtifactModel triggerArtifactModels = triggerArtifactRepository.findByTagAndBuildStatus(triggerManagerModel.getLatestTag(), BuildStatus.SUCCESS);
            if (triggerArtifactModel != null && triggerArtifactModel.buildStatus == BuildStatus.SUCCESS) {
                tags.add(triggerArtifactModel.getTag());
            }
        }
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @Operation(summary = "Download Artifact Image")
    @GetMapping("/delete/{artifactId}")
    public ResponseEntity<?> deleteArtifact(@PathVariable UUID artifactId) {
        // Get trigger object using triggerId
        if (!triggerArtifactRepository.existsById(artifactId)) {
            return new ResponseEntity<>("No artifactId with the id exist", HttpStatus.NOT_FOUND);
        }

        TriggerArtifactModel triggerArtifactModel = triggerArtifactRepository.getReferenceById(artifactId);

        String artifactFilePath = System.getenv("ARTIFACT_STORE") + "/artifacts/" + artifactId + "/" + artifactId + ".tar";
        File artifactFile = new File(artifactFilePath);
        if (artifactFile.exists()) {
            artifactFile.delete();
        }

        String artifactLogPath = System.getenv("ARTIFACT_STORE") + "/artifacts/" + artifactId + "/" + artifactId + ".log";
        File artifactLog = new File(artifactLogPath);
        if (artifactLog.exists()) {
            artifactLog.delete();
        }

        String artifactFolderPath = System.getenv("ARTIFACT_STORE") + "/artifacts/" + artifactId + "/";
        File artifactFolder = new File(artifactFolderPath);
        if (artifactFolder.exists()) {
            artifactFolder.delete();
        }

        if (triggerArtifactModel.getTriggerManagerModel().getBuildStatus() == BuildStatus.ACTIVE) {
            triggerArtifactModel.getTriggerManagerModel().setBuildStatus(BuildStatus.ABORTED);
            triggerArtifactRepository.save(triggerArtifactModel);
        }

        triggerArtifactRepository.delete(triggerArtifactModel);

        return new ResponseEntity<>("Artifact deleted.", HttpStatus.OK);

    }

}

