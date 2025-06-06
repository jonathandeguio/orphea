package io.bosler.build.controllers;


import io.bosler.build.library.dto.BuildPreviewResultRequest;
import io.bosler.build.library.dto.PostTransformRequest;
import io.bosler.build.library.dto.PreTransformRequest;
import io.bosler.build.library.dto.ResolveTargetRequest;
import io.bosler.build.library.services.BuildService;
import io.bosler.build.library.services.FunnelService;
import io.bosler.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/funnel")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Build", description = "Build service endpoints")
public class FunnelController {
    private final FunnelService funnelService;
    private final UserService userService;
    private final BuildService buildService;

    @Operation(summary = "Pre Transform for Pods")
    @PostMapping("/preTransform")
    public ResponseEntity<Object> preTransform(Principal principal,
                                               @RequestBody PreTransformRequest request
    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        HashMap<String, Object> response = funnelService.preTransform(request, userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Pre Transform for target")
    @PostMapping("/resolveTarget")
    public ResponseEntity<Object> preTransform(Principal principal,
                                               @RequestBody ResolveTargetRequest request
    ) throws Exception {
        UUID userId = request.getUserId() != null ? request.getUserId() : userService.getUser(principal.getName()).getId();
        HashMap<String, Object> response = funnelService.resolveTarget(request, userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Preview build result")
    @PostMapping("/previewPostTransform")
    public ResponseEntity<Object> previewPostTransform(Principal principal,
                                                       @RequestBody BuildPreviewResultRequest request
    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        UUID repoId = request.getRepositoryId();

        funnelService.previewPostTransform(request, userId, repoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Post Transform for Pods")
    @PostMapping("/postTransform")
    public ResponseEntity<Object> postTransform(Principal principal,
                                                @RequestBody PostTransformRequest request
    ) throws Exception {
        UUID userId = request.getUserId() != null ? request.getUserId() : userService.getUser(principal.getName()).getId();

        buildService.postTransform(
                request.getTarget(),
                request.getSources(),
                request.getTransactionId(),
                request.getBranch(),
                request.getRepositoryId(),
                request.getScriptPath(),
                request.getBuildId(),
                request.getBuildTrigger(),
                userId
        );

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
