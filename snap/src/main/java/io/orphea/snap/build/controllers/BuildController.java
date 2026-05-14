package io.orphea.snap.build.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.orphea.snap.build.library.enums.BuildStatus;
import io.orphea.snap.build.library.enums.BuildType;
import io.orphea.snap.build.library.models.TriggerArtifactModel;
import io.orphea.snap.build.library.models.TriggerManagerModel;
import io.orphea.snap.build.library.repository.TriggerArtifactRepository;
import io.orphea.snap.build.library.repository.TriggerRepository;
import io.orphea.snap.build.library.services.TriggerService;
import io.orphea.snap.git.library.repository.GitRepositoryRepository;
import io.orphea.snap.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/build")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Build", description = "Manage builds, images and triggers")
public class BuildController {
    private final UserService userService;
    private final TriggerRepository triggerRepository;
    private final TriggerArtifactRepository triggerArtifactRepository;
    private final TriggerService triggerService;
    private final GitRepositoryRepository gitRepositoryRepository;

    @Operation(summary = "Create Trigger")
    @PostMapping(path = "/trigger/create")
    public ResponseEntity<Object> createTrigger(Principal principal, @RequestBody TriggerManagerModel triggerManagerModel) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!triggerRepository.existsByName(triggerManagerModel.getName())) {
            TriggerManagerModel newTriggerManagerModel = new TriggerManagerModel();
            newTriggerManagerModel.setName(triggerManagerModel.getName());
            newTriggerManagerModel.setDescription(triggerManagerModel.getDescription());
            newTriggerManagerModel.setBranch(triggerManagerModel.getBranch());
            newTriggerManagerModel.setRepoName(triggerManagerModel.getRepoName());
            newTriggerManagerModel.setRepoUrl(triggerManagerModel.getRepoUrl());
            newTriggerManagerModel.setLatestTag(triggerManagerModel.getLatestTag());
            newTriggerManagerModel.setCommitId(triggerManagerModel.getCommitId());

            newTriggerManagerModel.setBuildType(triggerManagerModel.getBuildType());
            newTriggerManagerModel.setConfigFileName(triggerManagerModel.getConfigFileName());
            newTriggerManagerModel.setHarborProjectName(triggerManagerModel.getHarborProjectName());
            newTriggerManagerModel.setBuildAt(triggerManagerModel.getBuildAt());

            newTriggerManagerModel.setCreatedAt(new Date());
            newTriggerManagerModel.setUpdatedAt(new Date());
            newTriggerManagerModel.setCreatedBy(userId);

            triggerRepository.save(newTriggerManagerModel);
            return new ResponseEntity<>(newTriggerManagerModel, HttpStatus.OK);
        }
        return new ResponseEntity<>("Trigger already exists with name " + triggerManagerModel.getName(), HttpStatus.CONFLICT);
    }

    @Operation(summary = "Update Trigger")
    @PutMapping(path = "/trigger/update")
    public ResponseEntity<Object> updateTrigger(Principal principal, @RequestBody TriggerManagerModel triggerManagerModel) {
        UUID userId = userService.getUser(principal.getName()).getId();
        TriggerManagerModel existingTrigger = triggerRepository.getById(triggerManagerModel.getId());
        if (existingTrigger == null) {
            return new ResponseEntity<>("Trigger not found : " + triggerManagerModel.getId(), HttpStatus.NOT_FOUND);
        }
        existingTrigger.setDescription(triggerManagerModel.getDescription());
        existingTrigger.setBranch(triggerManagerModel.getBranch());

        existingTrigger.setBuildType(triggerManagerModel.getBuildType());
        existingTrigger.setConfigFileName(triggerManagerModel.getConfigFileName());
        existingTrigger.setHarborProjectName(triggerManagerModel.getHarborProjectName());

        existingTrigger.setUpdatedAt(new Date());
        existingTrigger.setUpdatedBy(userId);

        triggerRepository.save(existingTrigger);
        return new ResponseEntity<>(existingTrigger, HttpStatus.OK);
    }

    @Operation(summary = "Get Trigger by Id")
    @GetMapping("/trigger/{Id}")
    public ResponseEntity<Object> get(Principal principal, @PathVariable("Id") UUID Id) throws JsonProcessingException {
        UUID userId = userService.getUser(principal.getName()).getId();
        TriggerManagerModel triggerManagerModel = triggerRepository.getReferenceById(Id);
        if (!triggerRepository.existsById(Id)) {
            return new ResponseEntity<>("No Trigger with the id exist", HttpStatus.NOT_FOUND);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String respData = objectMapper.writeValueAsString(triggerManagerModel);

        return new ResponseEntity<>(respData, HttpStatus.OK);
    }

    @Operation(summary = "Delete Trigger.")
    @DeleteMapping("/trigger/{Id}")
    public ResponseEntity<Object> delete(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        // Delete the related scheduler jobs
        triggerRepository.deleteById(Id);
        return new ResponseEntity<>("Delete Success", HttpStatus.OK);
    }

    @Operation(summary = "Get all Triggers")
    @GetMapping("/trigger/all")
    public ResponseEntity<Object> getAllTriggers(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<TriggerManagerModel> triggerManagerModels = triggerRepository.findAllByOrderByNameAsc();

        return new ResponseEntity<>(triggerManagerModels, HttpStatus.OK);
    }

    @GetMapping("/trigger/registry-branch")
    public ResponseEntity<Object> getFilteredTrigger(
            Principal principal,
            @RequestParam(required = false) String registry,
            @RequestParam(required = false) String branch
    ) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<TriggerManagerModel> triggerManagerModels = null;
        List<String> registryList = registry != null ? Arrays.asList(registry.split(",")) : null;
        List<String> branchList = branch != null ? Arrays.asList(branch.split(",")) : null;
        System.out.println("registry: " + registryList + "\nbranch: " + branchList);
        triggerManagerModels = triggerRepository.findByRegistryAndBranch(registryList, branchList);
        return new ResponseEntity<>(triggerManagerModels, HttpStatus.OK);
    }

    @Operation(summary = "Get all Registries")
    @GetMapping("/trigger/registry")
    public ResponseEntity<Object> getAllRegistry(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<String> registries = triggerRepository.findAllHarborProjectName();

        return new ResponseEntity<>(registries, HttpStatus.OK);
    }

    @GetMapping("/trigger/branch")
    public ResponseEntity<Object> getAllBranch(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<String> branches = triggerRepository.findAllBranch();

        return new ResponseEntity<>(branches, HttpStatus.OK);
    }


    // Controller to Trigger the Build

    @Operation(summary = "Run trigger by Id")
    @GetMapping("/trigger/run/{Id}")
    public ResponseEntity<Object> buildImage(Principal principal, @PathVariable("Id") UUID triggerId) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId(); // Authenticate User

        Optional<TriggerManagerModel> optionalTriggerManagerModel = triggerRepository.findById(triggerId);

        if (optionalTriggerManagerModel.isEmpty()) {
            return new ResponseEntity<>("Trigger with ID " + triggerId + " not found.", HttpStatus.NOT_FOUND);
        }

        TriggerManagerModel triggerManagerModel = optionalTriggerManagerModel.get();

        boolean buildRunning = triggerManagerModel.getTriggerArtifactModels().stream()
                .anyMatch(artifactModel -> artifactModel.getBuildStatus() == BuildStatus.ACTIVE);

        if (buildRunning) {
            return new ResponseEntity<>("There is already an active build.", HttpStatus.OK);
        }

        triggerService.process(userId, triggerManagerModel);
        return new ResponseEntity<>("Build started successfully.", HttpStatus.OK);
    }

    @Operation(summary = "Run trigger by Id")
    @GetMapping("/trigger/runBuildAutomatic/{Id}")
    public ResponseEntity<Object> buildAutomaticImage(Principal principal, @PathVariable("Id") UUID triggerId) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId(); // Authenticate User

        Optional<TriggerManagerModel> optionalTriggerManagerModel = triggerRepository.findById(triggerId);

        if (optionalTriggerManagerModel.isEmpty()) {
            return new ResponseEntity<>("Trigger with ID " + triggerId + " not found.", HttpStatus.NOT_FOUND);
        }

        TriggerManagerModel triggerManagerModel = optionalTriggerManagerModel.get();
        if (triggerManagerModel.getBuildType() == BuildType.AUTOMATIC) {

            boolean buildRunning = triggerManagerModel.getTriggerArtifactModels().stream()
                    .anyMatch(artifactModel -> artifactModel.getBuildStatus() == BuildStatus.ACTIVE);

            if (buildRunning) {
                return new ResponseEntity<>("There is already an active build.", HttpStatus.OK);
            }

            triggerService.process(userId, triggerManagerModel);
            return new ResponseEntity<>("Build started successfully.", HttpStatus.OK);
        }
        return new ResponseEntity<>("Run via Snap as build is set to Manual", HttpStatus.OK);
    }
}