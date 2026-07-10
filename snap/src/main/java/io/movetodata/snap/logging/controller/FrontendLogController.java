package io.movetodata.snap.logging.controller;

import io.movetodata.snap.deployments.library.repository.DeploymentRepository;
import io.movetodata.snap.logging.library.models.FrontendLogModel;
import io.movetodata.snap.logging.library.services.LoggingService;
import io.movetodata.snap.passport.library.repository.UserRepository;
import io.movetodata.snap.passport.library.service.UserService;
import io.movetodata.snap.passport.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/logging")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Logging", description = "Manage Logs")
public class FrontendLogController {

    private final UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeploymentRepository deploymentRepository;

    private final LoggingService loggingService;

    @Operation(summary = "Manage Boson Frontend Logs")
    @PostMapping("/postFrontendLogs/{deploymentId}/{metricName}")
    public ResponseEntity<Object> generateFrontendLogs(Principal principal,
                                                       @RequestBody FrontendLogModel frontendLogModel,
                                                       @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricName") String metricName) {
        return loggingService.createCSV(principal, Collections.singletonList(frontendLogModel), deploymentId, "/frontend/errors", "Frontend.csv", FrontendLogModel.class, "frontend", metricName);
    }
}
