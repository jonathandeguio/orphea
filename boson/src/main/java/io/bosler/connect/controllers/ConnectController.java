package io.bosler.connect.controllers;

import io.bosler.connect.library.models.*;
import io.bosler.connect.library.requests.WebhookDTO;
import io.bosler.connect.library.responses.ConnectResponse;
import io.bosler.connect.library.services.ConnectService;
import io.bosler.connect.library.services.LinkService;
import io.bosler.connect.library.services.WebhookService;
import io.bosler.passport.library.Auth;
import io.bosler.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/connect")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Connect", description = "Code Repository management service endpoints")
public class ConnectController {
    private final ConnectService connectService;
    private final LinkService linkService;
    private final WebhookService webhookService;


    @Operation(summary = "Get connect config status.")
    @GetMapping("/config/{agentId}/status")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getConfigStatus(@PathVariable("agentId") UUID agentId) {
        ConnectConfig connectConfig = connectService.getConnectConfig(agentId);

        return new ResponseEntity<>(connectConfig, HttpStatus.OK);
    }

    @Operation(summary = "Pull connect config.")
    @GetMapping("/config/{agentId}/pull")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> configPull(@PathVariable("agentId") UUID agentId) {
        ConnectResponse connectResponse = connectService.getConnectResponse(agentId);

        return new ResponseEntity<>(connectResponse, HttpStatus.OK);
    }


    @Operation(summary = "Update connect config status.")
    @PostMapping("/config/{agentId}/agentStats")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> agentStatus(@PathVariable("agentId") UUID agentId, @RequestBody AgentStats agentStats) {
        AgentStats agentStats1 = connectService.getAgentStats(agentId, agentStats);

        return new ResponseEntity<>(agentStats1, HttpStatus.OK);
    }


    @Operation(summary = "Checks if the dataset is free to connect to a link.")
    @GetMapping("/isDatasetFreeToLink/{datasetId}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> isDatasetFreeToLink(@PathVariable("datasetId") UUID datasetId) {
        // TODO:Add some condition to check for transform connected dataset also
        return ResponseEntity.ok().body(!linkService.existsByDatasetId(datasetId));
    }

    @Operation(summary = "Get a webhook")
    @GetMapping("/webhook/{id}")
    public ResponseEntity<Object> getWebhook(@AuthenticationPrincipal AuthUser authUser, @PathVariable("id") UUID webhookId) {
        Webhook webhook = webhookService.findById(webhookId);
        return ResponseEntity.ok().body(webhook);
    }

    @Operation(summary = "Create a webhook")
    @PostMapping("/webhook/create")
    public ResponseEntity<Object> createWebhook(@AuthenticationPrincipal AuthUser authUser, @RequestBody WebhookDTO webhook) {
        UUID userId = authUser.getId();
        webhookService.createWebhook(webhook, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Put a webhook")
    @PostMapping("/webhook/update")
    public ResponseEntity<Object> updateWebhook(@AuthenticationPrincipal AuthUser authUser, @RequestBody WebhookDTO webhook) {
        UUID userId = authUser.getId();
        webhookService.updateWebhook(webhook, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Execute a webhook")
    @PostMapping("/webhook/execute/{id}")
    public ResponseEntity<Object> executeWebhook(@AuthenticationPrincipal AuthUser authUser, @PathVariable("id") UUID webhookId, @RequestBody List<RestAPIRequest> requests) {
        UUID userId = authUser.getId();
        List<WebhookCallData> calls = webhookService.executeWebhook(requests, webhookId, userId);
        return new ResponseEntity<>(calls, HttpStatus.OK);
    }

    @Operation(summary = "Get all webhook execution")
    @GetMapping("/webhook/executions/{id}")
    public ResponseEntity<Object> getWebhookExecutions(@AuthenticationPrincipal AuthUser authUser, @PathVariable("id") UUID webhookId) {
        UUID userId = authUser.getId();
        List<WebhookExecutionData> result = webhookService.getWebhookExecutions(webhookId, userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
