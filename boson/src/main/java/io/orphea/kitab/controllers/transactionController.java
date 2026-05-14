package io.orphea.kitab.controllers;

import io.orphea.build.library.repository.BuildLogRepository;
import io.orphea.kitab.library.repository.DatasetRepository;
import io.orphea.kitab.library.services.DatasetWritingTransactionService;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/transaction")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a dataset transactions management service.")
public class transactionController {

    private final UserService userService;
    private final AuthzService authzService;

    private final DatasetRepository datasetRepository;
    private final DatasetWritingTransactionService datasetWritingTransactionService;

    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private BuildLogRepository buildLogRepository;

    @Operation(summary = "Start transaction")
    @GetMapping("/{datasetId}/{branch}/start")
    public ResponseEntity<Object> startTransaction(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @RequestParam(name = "buildId", required = false) UUID buildId) throws Exception {
        if (!datasetRepository.existsById(datasetId)) {
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        datasetWritingTransactionService.startTransaction(datasetId, branch, userId, buildId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "End transaction")
    @GetMapping("/{datasetId}/{branch}/end")
    public ResponseEntity<Object> endTransaction(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) throws Exception {
        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        datasetWritingTransactionService.endTransaction(datasetId, branch, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Abort transaction")
    @GetMapping("/{datasetId}/{branch}/abort")
    public ResponseEntity<Object> abortTransaction(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) throws Exception {

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        datasetWritingTransactionService.abortTransaction(datasetId, branch, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Abort transactions for a build")
    @GetMapping("/{buildId}/abort")
    public ResponseEntity<Object> abortTransaction(Principal principal, @PathVariable("buildId") UUID buildId) {

        if (!buildLogRepository.existsById(buildId)) {
            return new ResponseEntity<>("No build log found for " + buildId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).getId();
        datasetWritingTransactionService.abortTransactionForBuild(buildId, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
