package io.orphea.snap.logging.controller;

import io.orphea.snap.deployments.library.models.DeploymentModel;
import io.orphea.snap.deployments.library.repository.DeploymentRepository;
import io.orphea.snap.logging.library.models.MetricsModel;
import io.orphea.snap.logging.library.services.LoggingService;
import io.orphea.snap.passport.library.repository.UserRepository;
import io.orphea.snap.passport.library.service.UserService;
import io.orphea.snap.passport.security.TokenProvider;
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
@RequestMapping("/api/logging")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Logging", description = "Manage Logs")
public class LoggingController {

    private final UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeploymentRepository deploymentRepository;

    private final LoggingService loggingService;

    @Operation(summary = "Get Last Updated TimeStamp")
    @GetMapping("/getLastLogTimeStamp/{deploymentId}/{metricType}")
    public ResponseEntity<Long> getLastUpdatedTimeStamp(Principal principal, @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricType") String metricType) {
        UUID userId = userService.getUser(principal.getName()).getId();
        try {
            if (!deploymentRepository.existsById(deploymentId)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            DeploymentModel deploymentModel = deploymentRepository.getReferenceById(deploymentId);

            Long timestampMillis = null;
            if (Objects.equals(metricType, "boson.applicationLog")) {
                timestampMillis = deploymentModel.getLastApplicationLogTimestamp();
            } else if (Objects.equals(metricType, "boson.metric.cpu")) {
                timestampMillis = deploymentModel.getLastCpuMetricLogTimestamp();
            } else if (Objects.equals(metricType, "boson.metric.memory")) {
                timestampMillis = deploymentModel.getLastMemoryMetricLogTimestamp();
            } else if (Objects.equals(metricType, "boson.metric.swap")) {
                timestampMillis = deploymentModel.getLastSwapMetricLogTimestamp();
            } else if (Objects.equals(metricType, "boson.metric.disk")) {
                timestampMillis = deploymentModel.getLastDiskMetricLogTimestamp();
            } else if (Objects.equals(metricType, "boson.accessLog")) {
                timestampMillis = deploymentModel.getLastAccessLogTimestamp();
            } else if (Objects.equals(metricType, "frontend.applicationLog")) {
                timestampMillis = deploymentModel.getFrontendLastApplicationLogTimestamp();
            } else if (Objects.equals(metricType, "capture.applicationLog")) {
                timestampMillis = deploymentModel.getCaptureLastApplicationLogTimestamp();
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (timestampMillis == null) {
                timestampMillis = 0L;
            }

            return new ResponseEntity<>(timestampMillis, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cpuMetricLogs/{deploymentId}/{metricType}")
    @Operation(summary = "Create systemCpuMetricLog CSV")
    public ResponseEntity<Object> createCSVForSystemCPUMetricsLogs(Principal principal,
                                                                   @RequestBody List<MetricsModel> requestMetricModels,
                                                                   @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricType") String metricType) {
        return loggingService.createCSV(principal, requestMetricModels, deploymentId, "/metric/cpu", "CpuMetrics.csv", MetricsModel.class, "boson", metricType);
    }

    @PostMapping("/memoryMetricLogs/{deploymentId}/{metricType}")
    @Operation(summary = "Create systemMemoryMetricLog CSV")
    public ResponseEntity<Object> createCSVForSystemMemoryMetricsLogs(Principal principal,
                                                                      @RequestBody List<MetricsModel> requestMetricModels,
                                                                      @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricType") String metricType) {
        return loggingService.createCSV(principal, requestMetricModels, deploymentId, "/metric/memory", "MemoryMetrics.csv", MetricsModel.class, "boson", metricType);
    }

    @PostMapping("/swapMetricLogs/{deploymentId}/{metricType}")
    @Operation(summary = "Create systemSwapMetricLog CSV")
    public ResponseEntity<Object> createCSVForSystemSwapMetricsLogs(Principal principal,
                                                                    @RequestBody List<MetricsModel> requestMetricModels,
                                                                    @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricType") String metricType) {
        return loggingService.createCSV(principal, requestMetricModels, deploymentId, "/metric/swap", "SwapMetrics.csv", MetricsModel.class, "boson", metricType);
    }

    @PostMapping("/diskMetricLogs/{deploymentId}/{metricType}")
    @Operation(summary = "Create systemMetricLog CSV")
    public ResponseEntity<Object> createCSVForSystemDiskMetricsLogs(Principal principal,
                                                                    @RequestBody List<MetricsModel> requestMetricModels,
                                                                    @PathVariable("deploymentId") UUID deploymentId, @PathVariable("metricType") String metricType) {
        return loggingService.createCSV(principal, requestMetricModels, deploymentId, "/metric/disk", "DiskMetrics.csv", MetricsModel.class, "boson", metricType);
    }
}
