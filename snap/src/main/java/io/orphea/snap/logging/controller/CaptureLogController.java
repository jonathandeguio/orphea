package io.orphea.snap.logging.controller;

import io.orphea.snap.deployments.library.repository.DeploymentRepository;
import io.orphea.snap.logging.library.models.ApplicationLogModel;
import io.orphea.snap.logging.library.services.LoggingService;
import io.orphea.snap.passport.library.repository.UserRepository;
import io.orphea.snap.passport.library.service.UserService;
import io.orphea.snap.passport.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/logging")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Logging", description = "Manage Logs")
public class CaptureLogController {

    private final UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeploymentRepository deploymentRepository;

    private final LoggingService loggingService;

    @Operation(summary = "Create applicationLog CSV")
    @PostMapping(path = "/captureApplicationLogs/{deploymentId}/{metricName}")
    public ResponseEntity<Object> createCSVForApplicationLogs(Principal principal,
                                                              @RequestBody List<ApplicationLogModel> applicationLogModels,
                                                              @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricName") String metricName) {
        return loggingService.createCSV(principal, applicationLogModels, deploymentId, "/application", "Application.csv", ApplicationLogModel.class, "capture", metricName);
    }
}
