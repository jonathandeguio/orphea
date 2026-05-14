package io.orphea.connect.controllers;

import io.orphea.build.library.models.BuildLog;
import io.orphea.connect.library.DTOs.LinkPreviewDTO;
import io.orphea.connect.library.models.Link;
import io.orphea.connect.library.requests.LinkRequestDTO;
import io.orphea.connect.library.services.LinkService;
import io.orphea.passport.library.Auth;
import io.orphea.passport.security.AuthUser;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/connect/link")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Connect", description = "Code Repository management service endpoints")
public class LinkController {
    private final LinkService linkService;

    @Operation(summary = "Get all links.")
    @GetMapping("/Getall")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getAll(@AuthenticationPrincipal AuthUser authUser) {
        UUID userId = authUser.getId();

        List<Link> linkList = linkService.getAllLinks(userId);

        return new ResponseEntity<>(linkList, HttpStatus.OK);
    }

    @Operation(summary = "It provides link by Id")
    @GetMapping("/GetById/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getById(@PathVariable("id") UUID id) {
        Link link = linkService.findById(id);

        return new ResponseEntity<>(link, HttpStatus.OK);
    }

    @Operation(summary = "It delete link by Id")
    @DeleteMapping("/DeleteById/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> deleteById(@PathVariable("id") UUID id) {
        linkService.deleteLinkById(id);

        // TODO : update config Status
//        ConnectConfig connectConfig = new ConnectConfig();
//        connectConfig.setAgentId(link1.getAgentId());
//        connectConfig.setVersion(UUID.randomUUID());
//        connectConfigRepository.save(connectConfig);

        return new ResponseEntity<>("Deleted Successfully. ", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create link.")
    @PostMapping("/create")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> newLink(@AuthenticationPrincipal AuthUser authUser, @RequestBody LinkRequestDTO link) throws Exception {
        UUID userId = authUser.getId();
        Link link1 = linkService.createLink(link, userId);

        return new ResponseEntity<>(link1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to update link.")
    @PostMapping("/update")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> update(@AuthenticationPrincipal AuthUser authUser, @RequestBody LinkRequestDTO link) {
        Link link1 = linkService.updateLink(link, authUser.getId());

        return new ResponseEntity<>(link1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to check if dataset already linked.")
    @GetMapping("/{datasetId}/{branch}/existsDatasetLink")
    public ResponseEntity<Object> existsDatasetLink(@PathVariable("datasetId") UUID datasetId,
                                                    @PathVariable("branch") String branch) {

        Map<String, Object> response = linkService.existsDatasetLinkCheck(datasetId, branch);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "This endpoint can be used to Build the link.")
    @GetMapping("/Build/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> build(@AuthenticationPrincipal AuthUser authUser, @PathVariable("id") UUID id) throws Exception {
        UUID userId = authUser.getId();
        BuildLog buildLog = linkService.getBuildLog(id, userId);

        return new ResponseEntity<>(buildLog, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to get the schema of a livelink.")
    @PostMapping("/schema/livelink/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> schema(@PathVariable("id") UUID id) throws Exception {
        return ResponseEntity.accepted().body(linkService.getLiveLinkSchema(id));
    }

    @Operation(summary = "This endpoint can be used to Preview a livelink.")
    @PostMapping("/preview/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> preview(@AuthenticationPrincipal AuthUser authUser, @PathVariable("id") UUID id, @RequestBody LinkPreviewDTO linkPreviewDTO) {
        UUID userId = authUser.getId();
        Map<String, Object> message = linkService.getPreview(id, linkPreviewDTO.getQuery(), linkPreviewDTO.getRequests(), linkPreviewDTO.getResponseParam(), linkPreviewDTO.getCsvPreprocessing(), false, userId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}

