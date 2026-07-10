package io.movetodata.dataset.controllers;

import io.movetodata.build.library.enums.WriteModeEnum;
import io.movetodata.build.library.models.BuildLog;
import io.movetodata.build.library.repository.BuildLogRepository;
import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.enums.DatasetMappingEnums;
import io.movetodata.dataset.library.models.DatasetMappingModel;
import io.movetodata.dataset.library.models.DatasetMappingTransactionModel;
import io.movetodata.dataset.library.repository.DatasetMappingRepository;
import io.movetodata.dataset.library.repository.DatasetMappingTransactionRepository;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.min;

@RestController
@RequestMapping("/api/dataset/datasetMapping")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dataset", description = "This is a Spark data management service.")
public class datasetMappingController {
    private final UserService userService;
    private final AuthzService authzService;
    private final DatasetMappingRepository datasetMappingRepository;
    private final DatasetMappingTransactionRepository datasetMappingTransactionRepository;
    private final DatasetMappingService datasetMappingService;
    private final BuildLogRepository buildLogRepository;

    @Operation(summary = "Get transaction details for dataset.")
    @GetMapping("/{datasetId}/{branch}")
    ResponseEntity<Object> getDatasetMapping(Principal principal,
                                             @PathVariable("datasetId") UUID datasetId,
                                             @PathVariable("branch") String branch) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!authzService.isViewer(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        DatasetMappingKey key = new DatasetMappingKey(datasetId, branch);

        if (datasetMappingRepository.existsById(key)) {
            DatasetMappingModel datasetMappingModel = datasetMappingRepository.findById(key).orElseThrow();
            return new ResponseEntity<>(datasetMappingModel, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get transaction.")
    @GetMapping("/transactions/{datasetId}/{branch}")
    ResponseEntity<Object> getDatasetMapping(Principal principal,
                                             @PathVariable("datasetId") UUID datasetId,
                                             @PathVariable("branch") String branch,
                                             @RequestParam(name = "date", required = false) String date) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!authzService.isViewer(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        DatasetMappingKey key = new DatasetMappingKey(datasetId, branch);

        if (datasetMappingRepository.existsById(key)) {
            DatasetMappingModel datasetMappingModel = datasetMappingRepository.findById(key).orElseThrow();
            if (date == null) {
                List<DatasetMappingTransactionModel> transactions = datasetMappingTransactionRepository.findByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
                if (transactions.isEmpty()) {
                    return new ResponseEntity<>(transactions, HttpStatus.OK);
                }
                return new ResponseEntity<>(transactions.subList(0, min(transactions.size(), 10)), HttpStatus.OK);
            }
            List<DatasetMappingTransactionModel> transactions = datasetMappingTransactionRepository.findByDatasetIdAndBranchAndLocalDateOrderByCreatedAtDesc(datasetId, branch, LocalDate.parse(date));
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Create new transaction for dataset.")
    @GetMapping("/createTransaction/{datasetId}/{branch}/{buildId}")
    ResponseEntity<Object> createTransaction(Principal principal,
                                             @PathVariable("datasetId") UUID datasetId,
                                             @PathVariable("branch") String branch,
                                             @PathVariable("buildId") UUID buildId) throws Exception {

        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!authzService.isEditor(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        BuildLog buildLog = buildLogRepository.getReferenceById(buildId);
        UUID newTransactionId = datasetMappingService.createTransaction(datasetId, branch, userId, buildLog.getLaunchedBy(), buildId, WriteModeEnum.SNAPSHOT);

        HashMap<String, Object> response = new HashMap<>();
        response.put("transactionId", newTransactionId.toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "Update dataset history source and count.")
    @PutMapping("/update/{datasetId}/{branch}/{source}/{count}")
    ResponseEntity<Object> updateDatasetHistorySourceAndCount(Principal principal,
                                                              @PathVariable("datasetId") UUID datasetId,
                                                              @PathVariable("branch") String branch,
                                                              @PathVariable("source") String source,
                                                              @PathVariable("count") Integer count
    ) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!authzService.isPlatformAdmin(userId))
            throw new UnauthorizedException();

        if (!authzService.isEditor(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        DatasetMappingKey key = new DatasetMappingKey(datasetId, branch);
        DatasetMappingModel datasetMappingModel = datasetMappingRepository.getReferenceById(key);
        datasetMappingModel.setHistoryStoreType(DatasetMappingEnums.valueOf(source));
        datasetMappingModel.setDatasetHistory(count);
        datasetMappingRepository.save(datasetMappingModel);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Abort dataset transaction.")
    @PostMapping("/{datasetId}/{transactionId}/abort")
    ResponseEntity<Object> abortDatasetTransaction(Principal principal,
                                                   @PathVariable("datasetId") UUID datasetId,
                                                   @PathVariable("transactionId") UUID transactionId
    ) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (transactionId != null) {
            datasetMappingService.forceClosureOfTransaction(transactionId, userId);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

