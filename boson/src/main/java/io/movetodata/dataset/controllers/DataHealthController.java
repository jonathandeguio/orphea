package io.movetodata.dataset.controllers;

import io.movetodata.dataset.library.DTOs.DataHealthDTO;
import io.movetodata.dataset.library.models.DataHealth.DataHealthLogModel;
import io.movetodata.dataset.library.models.DataHealth.DataHealthModel;
import io.movetodata.dataset.library.services.DataHealth.DataHealthLogService;
import io.movetodata.dataset.library.services.DataHealth.DataHealthService;
import io.movetodata.passport.library.Auth;
import io.movetodata.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "DataHealth", description = "Data Health")
public class DataHealthController {
    private final DataHealthService dataHealthService;
    private final DataHealthLogService dataHealthLogService;

    @Operation(summary = "Put a data health check")
    @PutMapping("/{datasetId}/{branch}")
    @PreAuthorize(Auth.EDITOR)
    ResponseEntity<Object> putHealthCheck(@AuthenticationPrincipal AuthUser authUser,
                                          @PathVariable("datasetId") @Param("id") UUID datasetId, @PathVariable("branch") String branch, @RequestBody DataHealthDTO dataHealthDTO) throws Exception {
        UUID userId = authUser.getId();

        dataHealthService.put(datasetId, branch, dataHealthDTO, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get data health checks")
    @GetMapping("/{datasetId}/{branch}")
    @PreAuthorize(Auth.VIEWER)
    ResponseEntity<Object> getHealthCheck(@AuthenticationPrincipal AuthUser authUser,
                                          @PathVariable("datasetId") @Param("id") UUID datasetId, @PathVariable("branch") String branch) throws Exception {
        List<DataHealthModel> dataHealthModelList = dataHealthService.get(datasetId, branch);
        return new ResponseEntity<>(dataHealthModelList, HttpStatus.OK);
    }

    @Operation(summary = "Get health check logs for health check id")
    @GetMapping("/logs/{id}/{healthCheckId}")
    @PreAuthorize(Auth.VIEWER)
    ResponseEntity<Object> getHealthCheckLogs(@AuthenticationPrincipal AuthUser authUser,
                                              @PathVariable("id") UUID id,
                                              @PathVariable("healthCheckId") UUID healthCheckId) throws Exception {
        List<DataHealthLogModel> dataHealthLogModelList = dataHealthLogService.getLogs(healthCheckId);
        return new ResponseEntity<>(dataHealthLogModelList, HttpStatus.OK);
    }

    @Operation(summary = "Delete a health check")
    @DeleteMapping("/{id}")
    @PreAuthorize(Auth.EDITOR)
    ResponseEntity<Object> getHealthCheck(@AuthenticationPrincipal AuthUser authUser,
                                          @PathVariable("id") @Param("id") UUID id) throws Exception {
        dataHealthService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}