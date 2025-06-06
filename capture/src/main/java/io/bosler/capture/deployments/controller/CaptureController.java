package io.bosler.capture.deployments.controller;

import io.bosler.capture.deployments.library.model.EnvConfig;
import io.bosler.capture.deployments.library.services.UpdateChecker;
import io.bosler.capture.deployments.library.services.UpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capture")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dataset", description = "This is a Spark data management service.")
public class CaptureController {

    private final UpdateService updateService;

    @Operation(summary = "This can be used to check for updates.")
    @GetMapping("/checkUpdates")
    public ResponseEntity<String> checkUpdates() {
        try {
            updateService.checkUpdates();
            return new ResponseEntity<>("Ok", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
