package io.bosler.snap.deployments.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.bosler.snap.deployments.library.Enums.DeploymentMethod;
import io.bosler.snap.deployments.library.models.ConfigurationComponentsModel;
import io.bosler.snap.deployments.library.models.DeploymentModel;
import io.bosler.snap.deployments.library.repository.ConfigurationComponentsRepository;
import io.bosler.snap.deployments.library.repository.DeploymentRepository;
import io.bosler.snap.deployments.library.requests.DeploymentRequestModel;
import io.bosler.snap.deployments.library.services.DeploymentService;
import io.bosler.snap.passport.library.repository.UserRepository;
import io.bosler.snap.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.bosler.snap.passport.security.TokenProvider;

import java.security.Principal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/deployments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Deployments", description = "Manage Deployments")
public class DeploymentController {
    private final UserService userService;
    private final DeploymentService deploymentService;
    private final DeploymentRepository deploymentRepository;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConfigurationComponentsRepository configurationComponentsRepository;

    @Operation(summary = "Create Deployment")
    @PostMapping(path = "/create")
    public ResponseEntity<Object> create(Principal principal, @RequestBody DeploymentRequestModel deploymentRequestModel) {
        System.out.println("creating");
        UUID userId = userService.getUser(principal.getName()).getId();
        DeploymentModel newDeploymentModel = new DeploymentModel();
        newDeploymentModel.setName(deploymentRequestModel.getName());
        newDeploymentModel.setLocation(deploymentRequestModel.getLocation());
        newDeploymentModel.setAddress(deploymentRequestModel.getAddress());
        newDeploymentModel.setContactDetails(deploymentRequestModel.getContactDetails());
        newDeploymentModel.setEmail(deploymentRequestModel.getEmail());

        if (Objects.equals(deploymentRequestModel.getDeploymentMethod(), DeploymentMethod.AUTOMATIC)) {
            newDeploymentModel.setDeploymentMethod(DeploymentMethod.AUTOMATIC);
        }
        if (Objects.equals(deploymentRequestModel.getDeploymentMethod(), DeploymentMethod.MANUAL)) {
            newDeploymentModel.setDeploymentMethod(DeploymentMethod.MANUAL);
        }

        newDeploymentModel.setCreatedAt(new Date());
        newDeploymentModel.setCreatedBy(userId);
        newDeploymentModel.setTimeWindowStart(deploymentRequestModel.getTimeWindowStart());
        newDeploymentModel.setTimeWindowEnd(deploymentRequestModel.getTimeWindowEnd());
        newDeploymentModel.setBranch(deploymentRequestModel.getBranch());

        DeploymentModel deploymentModelSaved = deploymentRepository.save(newDeploymentModel);
        return new ResponseEntity<>(deploymentModelSaved, HttpStatus.OK);
    }

    @Operation(summary = "Get Trigger by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> get(Principal principal, @PathVariable("Id") UUID Id) throws JsonProcessingException {
        UUID userId = userService.getUser(principal.getName()).getId();
        DeploymentModel deploymentModel = deploymentRepository.getReferenceById(Id);
        if (!deploymentRepository.existsById(Id)) {
            return new ResponseEntity<>("No Deployment with the id exist", HttpStatus.NOT_FOUND);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String respData = objectMapper.writeValueAsString(deploymentModel);

        return new ResponseEntity<>(respData, HttpStatus.OK);
    }

    @Operation(summary = "Delete Trigger.")
    @DeleteMapping("/{Id}")
    public ResponseEntity<Object> delete(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        // Delete the related scheduler jobs
        deploymentRepository.deleteById(Id);
        return new ResponseEntity<>("Delete Success", HttpStatus.OK);
    }

    @Operation(summary = "Get all Triggers")
    @GetMapping("/all")
    public ResponseEntity<Object> getAll(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<DeploymentModel> deploymentModels = deploymentRepository.findAll();

        return new ResponseEntity<>(deploymentModels, HttpStatus.OK);
    }

    @Operation(summary = "Update Trigger by Id")
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(Principal principal, @PathVariable("id") UUID deploymentId, @RequestBody DeploymentRequestModel deploymentRequestModel) throws JsonProcessingException {
        UUID userId = userService.getUser(principal.getName()).getId();

        DeploymentModel deploymentModel = deploymentRepository.getReferenceById(deploymentId);
        if (!deploymentRepository.existsById(deploymentId)) {
            return new ResponseEntity<>("No Deployment with the id exist", HttpStatus.NOT_FOUND);
        }
        deploymentModel.setName(deploymentRequestModel.getName());
        deploymentModel.setLocation(deploymentRequestModel.getLocation());
        deploymentModel.setAddress(deploymentRequestModel.getAddress());
        deploymentModel.setContactDetails(deploymentRequestModel.getContactDetails());
        deploymentModel.setEmail(deploymentRequestModel.getEmail());
        if (Objects.equals(deploymentRequestModel.getDeploymentMethod(), DeploymentMethod.AUTOMATIC) && deploymentRequestModel.getPausedUntil() == null) {
            deploymentModel.setPausedUntil(null);
            deploymentModel.setDeploymentMethod(DeploymentMethod.AUTOMATIC);
        }
        if (Objects.equals(deploymentRequestModel.getDeploymentMethod(), DeploymentMethod.MANUAL)) {
            deploymentModel.setDeploymentMethod(DeploymentMethod.MANUAL);
        }
        if (Objects.equals(deploymentRequestModel.getDeploymentMethod(), DeploymentMethod.AUTOMATIC) && deploymentRequestModel.getPausedUntil() != null) {
            deploymentModel.setPausedUntil(deploymentRequestModel.getPausedUntil());
//            deploymentModel.setDeploymentMethod(DeploymentMethod.PAUSE);
        }
        deploymentModel.setDeploymentMethod(deploymentRequestModel.getDeploymentMethod());
        deploymentModel.setTimeWindowStart(deploymentRequestModel.getTimeWindowStart());
        deploymentModel.setTimeWindowEnd(deploymentRequestModel.getTimeWindowEnd());
        List<ConfigurationComponentsModel> componentsModel = deploymentRequestModel.getConfigurationComponentsModel();
        List<ConfigurationComponentsModel> newComp = new ArrayList<>();

        if (deploymentRequestModel.getOverRideHours() != null) {
            ZonedDateTime zoneNow = ZonedDateTime.now(ZoneId.of("Europe/London"));
            LocalTime now = zoneNow.toLocalTime();
            LocalTime newTime = now.plusHours(deploymentRequestModel.getOverRideTimeWindow());

            long overrideTime = newTime.toNanoOfDay() / 1_000_000;

            deploymentModel.setOverRideTimeWindow(overrideTime);
        }
        deploymentModel.setBranch(deploymentRequestModel.getBranch());

        deploymentRepository.save(deploymentModel);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String respData = objectMapper.writeValueAsString(deploymentModel);
        return new ResponseEntity<>(respData, HttpStatus.OK);
    }

    @Operation(summary = "Get all Triggers")
    @GetMapping("/globalVersionByDeploymentId/{id}")
    public ResponseEntity<String> getLatestGlobalVersion(Principal principal, @PathVariable("id") UUID deploymentId) {
        UUID userId = userService.getUser(principal.getName()).getId();
        DeploymentModel deploymentModel = deploymentRepository.getReferenceById(deploymentId);
        if (!deploymentRepository.existsById(deploymentId)) {
            return new ResponseEntity<>("No Deployment with the id exist", HttpStatus.NOT_FOUND);
        }
        ;
        List<ConfigurationComponentsModel> configurationComponentsModel = deploymentModel.getConfigurationComponentsModel();
        if (configurationComponentsModel == null) {
            return new ResponseEntity<>("0.0.1", HttpStatus.NOT_FOUND);
        }
        for (ConfigurationComponentsModel componentsModel : configurationComponentsModel) {
            if (Objects.equals(componentsModel.getState().getDescription(), "ACTIVE") || Objects.equals(componentsModel.getState().getDescription(), "active")) {
                return new ResponseEntity<>(componentsModel.getGlobalVersion(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Global Version not Found", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update branch for each configuration by deployment Id")
    @PostMapping("/updateComponentConfiguration/{deploymentId}")
    public ResponseEntity<?> updateComponentConfig(@PathVariable("deploymentId") UUID deploymentID) {
        DeploymentModel deploymentModel = deploymentRepository.getReferenceById(deploymentID);
        if (!deploymentRepository.existsById(deploymentID)) {
            return new ResponseEntity<>("No Deployment found for Id : " + deploymentID, HttpStatus.NOT_FOUND);
        }
        for (ConfigurationComponentsModel componentsModel : deploymentModel.getConfigurationComponentsModel()) {
            componentsModel.setBranch(deploymentModel.getBranch());
            configurationComponentsRepository.save(componentsModel);
        }
        return new ResponseEntity<>("Deployment configuration updated", HttpStatus.OK);
    }


}