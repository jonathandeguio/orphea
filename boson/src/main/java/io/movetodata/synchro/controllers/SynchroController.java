package io.movetodata.synchro.controllers;

import io.movetodata.passport.library.Auth;
import io.movetodata.passport.security.AuthUser;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import io.movetodata.synchro.DTOs.SyncPropertiesDTO;
import io.movetodata.synchro.library.models.SyncSpecification;
import io.movetodata.synchro.library.services.DataMartService;
import io.movetodata.synchro.library.services.SynchroService;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/synchro")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Synchro", description = "This is to synchronise spark dataframes to databases.")
public class SynchroController {
    private final SynchroService synchroService;
    private final DataMartService dataMartService;

    @Operation(summary = "Create or Update sync.")
    @PutMapping("/sync")
    @PreAuthorize("isEditor(#syncProperties.datasetId)")
    public ResponseEntity<Object> create(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody SyncPropertiesDTO syncProperties
    ) throws Exception {
        UUID userId = authUser.getId();
        SyncSpecification syncSpecification = synchroService.putSync(syncProperties, userId);
        return new ResponseEntity<>(syncSpecification, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Delete sync.")
    @DeleteMapping("/sync/{datasetId}/{syncId}")
    @PreAuthorize(Auth.EDITOR)
    public ResponseEntity<Object> delete(
            @PathVariable("datasetId") @Param("id") UUID datasetId,
            @PathVariable UUID syncId
    ) throws Exception {
        synchroService.deleteSync(syncId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get syncs.")
    @GetMapping("/sync/{datasetId}/{branch}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> getDatasetSyncs(@PathVariable("datasetId") @Param("id") UUID datasetId, @PathVariable("branch") String branch) {
        List<SyncSpecification> syncs = synchroService.getDatasetSync(datasetId, branch);
        return new ResponseEntity<>(syncs, HttpStatus.OK);
    }

    @Operation(summary = "Get sync.")
    @GetMapping("/sync/{syncId}")
    public ResponseEntity<Object> getSync(@PathVariable("syncId") UUID syncId) {
        Optional<SyncSpecification> sync = synchroService.getSync(syncId);

        if (sync.isEmpty()) {
            throw new ResourceNotFoundException("Sync not found with id " + syncId);
        }
        SyncSpecification syncSpec = sync.get();
        return new ResponseEntity<>(syncSpec, HttpStatus.OK);
    }

    @Operation(summary = "Perform sync.")
    @PostMapping("/perform/{datasetId}/{syncId}")
    @PreAuthorize(Auth.EDITOR)
    public ResponseEntity<Object> perform(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("datasetId") @Param("id") UUID datasetId,
            @PathVariable UUID syncId
    ) throws Exception {
        UUID userId = authUser.getId();
        synchroService.manualSync(syncId, userId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/getUnInitializedDataMarts/{datasetId}/{branch}")
    public ResponseEntity<Object> getProjectDataMartConfig(@PathVariable UUID datasetId, @PathVariable String branch) {
        List<SyncSpecification> dataMartSyncSpecs = dataMartService.getUnInitializedDatasetDataMarts(datasetId, branch);
        return new ResponseEntity<>(dataMartSyncSpecs, HttpStatus.OK);
    }
}
