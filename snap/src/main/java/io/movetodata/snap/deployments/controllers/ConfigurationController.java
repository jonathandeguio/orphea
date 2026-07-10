package io.movetodata.snap.deployments.controllers;

import io.movetodata.snap.build.library.repository.TriggerRepository;
import io.movetodata.snap.deployments.library.Enums.ConfigurationState;
import io.movetodata.snap.deployments.library.models.ConfigurationComponentsModel;
import io.movetodata.snap.deployments.library.models.DeploymentModel;
import io.movetodata.snap.deployments.library.repository.ConfigurationComponentsRepository;
import io.movetodata.snap.deployments.library.repository.DeploymentRepository;
import io.movetodata.snap.deployments.library.requests.DeploymentStateRequestModel;
import io.movetodata.snap.deployments.library.services.DeploymentService;
import io.movetodata.snap.passport.library.repository.UserRepository;
import io.movetodata.snap.passport.library.service.UserService;
import io.movetodata.snap.passport.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/deployments/configuration")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Deployments", description = "Manage Configuration")
public class ConfigurationController {
    private final UserService userService;

    private final ConfigurationComponentsRepository configurationComponentsRepository;
    private final DeploymentRepository deploymentRepository;
    private final TriggerRepository triggerRepository;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeploymentService deploymentService;


    @Operation(summary = "Update configuration (This will be only used by automatic client service)")
    @PutMapping(path = "/state/active/{deploymentId}")
    public ResponseEntity<Object> updateCurrentState(Principal principal,
                                                     @PathVariable("deploymentId") UUID deploymentId,
                                                     @RequestBody DeploymentStateRequestModel deploymentStateRequestModel
    ) {
        try {
            UUID userId = userService.getUser(principal.getName()).getId();
            boolean process = deploymentService.processInTimeWindow(deploymentId, deploymentStateRequestModel, ConfigurationState.ACTIVE);
            if (!process) {
                return new ResponseEntity<>("Time Out Of Window Can not deploy", HttpStatus.CONFLICT);
            }

        } catch (Error e) {
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Update Active configuration (This will be used by SNAP frontend)")
    @PutMapping(path = "/state/target/{deploymentId}")
    public ResponseEntity<Object> updateTargetState(Principal principal,
                                                    @PathVariable("deploymentId") UUID deploymentId,
                                                    @RequestBody DeploymentStateRequestModel deploymentStateRequestModel
    ) {
        try {
            UUID userId = userService.getUser(principal.getName()).getId();
            boolean process = deploymentService.processInTimeWindow(deploymentId, deploymentStateRequestModel, ConfigurationState.TARGET);
            if (!process) {
                return new ResponseEntity<>("Time Out Of Window Can not deploy", HttpStatus.CONFLICT);
            }
        } catch (Error e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>("Updated", HttpStatus.OK);
    }


    @Operation(summary = "Check the current target state of deployment components (This will be only used by automatic client service )")
    @GetMapping(path = "/check/target/state/{deploymentId}")
    public ResponseEntity<Object> checkTargetState(@PathVariable("deploymentId") UUID deploymentId) {
        try {
            DeploymentModel deployment = deploymentRepository.findById(deploymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Deployment not found"));

            ConfigurationComponentsModel targetStateComponent = deployment.getConfigurationComponentsModel().stream()
                    .filter(component -> component.getState() == ConfigurationState.TARGET)
                    .findFirst()
                    .orElse(null);

            if (targetStateComponent == null) {
                return new ResponseEntity<>("No TARGET state component found", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(targetStateComponent, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error fetching target state", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete by global Version")
    @DeleteMapping(path = "/deleteByGlobalVersion/{globalVersion}")
    public ResponseEntity<Object> delete(Principal principal, @PathVariable("globalVersion") String globalVersion) {
        List<ConfigurationComponentsModel> componentsModel = configurationComponentsRepository.findByGlobalVersion(globalVersion);
        if (componentsModel == null) {
            return new ResponseEntity<>("No Component Exists", HttpStatus.NOT_FOUND);
        }
        configurationComponentsRepository.deleteAll(componentsModel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}