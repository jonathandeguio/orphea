package io.movetodata.dataset.controllers;


import io.movetodata.build.library.models.SocketMessage;
import io.movetodata.dataset.library.DTOs.ColumnDTO;
import io.movetodata.dataset.library.DTOs.CustomSchemaApplyResultDTO;
import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.models.DatasetStatsResponse;
import io.movetodata.dataset.library.models.RenameModel;
import io.movetodata.dataset.library.models.SchemaApplyModel;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.dataset.library.repository.DatasetMappingRepository;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.dataset.library.services.DatasetService;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.kitab.library.models.DatasetStatsModel;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.DatasetStatsRepository;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.Auth;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.platform.library.repository.CacheRepository;
import io.movetodata.sharedutils.BackingFS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static io.movetodata.dataset.library.services.DatasetService.hasSchemaChanged;
import static io.movetodata.sharedutils.Redis.clearRelatedCaches;


@RestController
@RequestMapping("/api/dataset/schema")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dataset", description = "This is a data management service.")
public class schemaController {

    private final DatasetService datasetService;
    private final DatasetRepository datasetRepository;
    private final DatasetStatsRepository datasetStatsRepository;
    private final SchemaRepository schemaRepository;
    private final CacheRepository cacheRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final DatasetMappingRepository datasetMappingRepository;
    private final DatasetMappingService datasetMappingService;

    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private SparkDataService sparkDataService;
    @Autowired
    private ResourceService resourceService;


    @Operation(summary = "Compares Two Dataset Schema.")
    @GetMapping("/{sourceDataset}/{destinationDataset}/{sourceBranch}/{destinationBranch}/compareSchema")
    @PreAuthorize(Auth.EDITOR)
    ResponseEntity<Object> areSameDataset(@PathVariable("sourceDataset") @Param("id") UUID sourceDatasetId,
                                          @PathVariable("destinationDataset") UUID destinationDatasetId,
                                          @PathVariable("sourceBranch") String sourceBranch,
                                          @PathVariable("destinationBranch") String destinationBranch) throws Exception {

        UUID sourceTransaction = datasetMappingService.getDatasetMapping(sourceDatasetId, sourceBranch).get().getCurrentTransaction();
        UUID destinationTransaction = datasetMappingService.getDatasetMapping(destinationDatasetId, destinationBranch).get().getCurrentTransaction();

        SchemaModel sourceSchema = sparkDataService.findByDatasetIdAndBranchAndTransactionIdAndStatus(sourceDatasetId, sourceBranch, sourceTransaction);
        SchemaModel destinationSchema = sparkDataService.findByDatasetIdAndBranchAndTransactionIdAndStatus(destinationDatasetId, destinationBranch, destinationTransaction);

        if (Objects.isNull(destinationSchema) || Objects.isNull(sourceSchema)) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

        Set<String> sourceSet = Arrays.stream(sourceSchema.getSchema().fields()).map(structField -> structField.name() + structField.dataType().toString()).collect(Collectors.toSet());
        Set<String> destinationSet = Arrays.stream(destinationSchema.getSchema().fields()).map(structField -> structField.name() + structField.dataType().toString()).collect(Collectors.toSet());

        sourceSet.removeAll(destinationSet);

        return new ResponseEntity<>(sourceSet.isEmpty(), HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be read schema")
    @GetMapping("/{datasetId}/{branch}/{transactionId}")
    ResponseEntity<Object> schema(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);

        }

        Map<String, Object> result = sparkDataService.getDatasetSchemaForFunnel(datasetId, transactionId, branch);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to get dataset columns")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/columns")
    @PreAuthorize(Auth.VIEWER)
    ResponseEntity<Object> columns(Principal principal, @PathVariable("datasetId") @Param("id") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {
        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        List<ColumnDTO> columnDTOS = sparkDataService.getDatasetColumns(datasetId, transactionId, branch);
        // columns dict
        return ResponseEntity.accepted().body(columnDTOS);
    }

    @Operation(summary = "This endpoint can be used to calculate datasets stats")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/calculateStats")
    ResponseEntity<Object> calculateStats(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, datasetId)) {
            throw new UnauthorizedException();
        }


        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");

        template.convertAndSend("/topic/statsCalculation/" + datasetId + "/" + branch, textMessage);

        datasetService.statsCalculation(userId, datasetId, branch, transactionId);

        return new ResponseEntity<>("Stats Calculation in progress", HttpStatus.OK);
    }


    @Operation(summary = "This endpoint can be used to get dataset stats")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/stats")
    ResponseEntity<Object> stats(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, datasetId)) {
            throw new UnauthorizedException();
        }


        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        DatasetStatsResponse datasetStatsResponse = new DatasetStatsResponse();

        ResourceModel datasetInfo = resourceService.getResourceModel(datasetId);

        datasetStatsResponse.setResourceModel(datasetInfo);

        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(datasetId, branch);

            datasetStatsResponse.setDatasetStatsModel(datasetStatsModel);

            return new ResponseEntity<>(datasetStatsResponse, HttpStatus.OK);
        } else {

            Dataset<Row> dfTotal = sparkDataService.getSparkDF(datasetId, branch, transactionId, 0);

//            long totalRows = dfTotal.count(); // gives number of rows
            long totalColumns = dfTotal.columns().length; // gives number of rows

            List<Map<String, Object>> listOfFiles = BackingFS.getListOfFiles(datasetId, branch, transactionId);

            int numFiles = listOfFiles.size();

            long dfSize = 0;

            for (Map<String, Object> files : listOfFiles) {
                long sizeInBytes = (long) files.get("sizeInBytes");
                dfSize += sizeInBytes;
            }

            DatasetStatsModel datasetStatsModel = new DatasetStatsModel();
            datasetStatsModel.setSize(dfSize);
            datasetStatsModel.setColumns(totalColumns);
            datasetStatsModel.setFiles(numFiles);

            datasetStatsResponse.setDatasetStatsModel(datasetStatsModel);

            return new ResponseEntity<>(datasetStatsResponse, HttpStatus.OK);

        }

//        return new ResponseEntity<>("No dataset statistics found.", HttpStatus.OK);

    }

    @Operation(summary = "This endpoint can be used to apply schema to existing datasets")
    @PostMapping("/{datasetId}/{branch}/{transactionId}/test")
    ResponseEntity<Object> test(Principal principal, @RequestBody SchemaApplyModel complexSchema, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingService.getDatasetMapping(datasetId, branch).get().getCurrentTransaction();
        }

        if (!datasetMappingRepository.getReferenceById(new DatasetMappingKey(datasetId, branch)).getCurrentTransaction().equals(transactionId)) {
            return new ResponseEntity<>("Not allowed to change historical data", HttpStatus.FORBIDDEN);
        }

//        sparkDataService.exampleConvert();
//        sparkDataService.convertTransactionDate(sparkDataService.getSparkDF(datasetId, branch, transactionId, -1), datasetId, transactionId);
//        sparkDataService.convertColumnWrapper(datasetId, branch, transactionId, "timestamp", "Transaction_date", "");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to apply schema to existing datasets")
    @PostMapping("/{datasetId}/{branch}/{transactionId}/apply")
    ResponseEntity<Object> applySchema(Principal principal, @RequestBody SchemaApplyModel complexSchema, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingService.getDatasetMapping(datasetId, branch).get().getCurrentTransaction();
        }

        if (!datasetMappingRepository.getReferenceById(new DatasetMappingKey(datasetId, branch)).getCurrentTransaction().equals(transactionId)) {
            return new ResponseEntity<>("Not allowed to change historical data", HttpStatus.FORBIDDEN);
        }


        SchemaModel schemaModelActiveExisting = sparkDataService.findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId);

        schemaModelActiveExisting.setUpdatedBy(userId);
        schemaModelActiveExisting.setUpdatedAt(new Date());


        if (hasSchemaChanged(schemaModelActiveExisting, complexSchema)) {
            CustomSchemaApplyResultDTO customSchemaApplyResult = sparkDataService.handleCustomSchemaApply(datasetId, branch, transactionId, complexSchema.getCustomSchema());

            StructType schema = customSchemaApplyResult.getSchema();
            complexSchema.getCustomSchema().setDateFormat(customSchemaApplyResult.getDateFormatMap());
            schemaModelActiveExisting.setCustomSchema(complexSchema.getCustomSchema());
            schemaModelActiveExisting.setSchema(schema);

        } else {
            sparkDataService.handleChangedColumnsTypes(schemaModelActiveExisting.getSchema(), complexSchema.getSchema(), datasetId, branch, transactionId, schemaModelActiveExisting.getSchema(), complexSchema.getCustomSchema());
            schemaModelActiveExisting.setCustomSchema(complexSchema.getCustomSchema());
            schemaModelActiveExisting.setSchema(complexSchema.getSchema());
        }

        // Remove Redis cache
        clearRelatedCaches(datasetId, branch, cacheRepository);

        schemaRepository.save(schemaModelActiveExisting);

        Map<String, Object> result = new HashMap<>();

        // TODO SCHEMA : get latest schema
        result.put("schema", schemaModelActiveExisting.getSchema());
        result.put("customSchema", schemaModelActiveExisting.getCustomSchema());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to rename column names")
    @PostMapping("/{datasetId}/{branch}/{transactionId}/rename")
    ResponseEntity<Object> rename(Principal principal, @PathVariable("datasetId") UUID datasetId, @RequestBody RenameModel renameModel, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, datasetId)) {
            throw new UnauthorizedException();
        }

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingService.getDatasetMapping(datasetId, branch).get().getCurrentTransaction();
        }

        if (!datasetMappingRepository.getReferenceById(new DatasetMappingKey(datasetId, branch)).getCurrentTransaction().equals(transactionId)) {
            return new ResponseEntity<>("Not allowed to change historical data", HttpStatus.FORBIDDEN);
        }
        SchemaModel schemaModelActiveExisting = sparkDataService.findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId);

        StructType oldSchema = schemaModelActiveExisting.getSchema();
        StructType newSchema = new StructType();

        for (StructField element : oldSchema.fields()) {
            if (element.name().equals(renameModel.getColumnName())) {
                if (renameModel.getNewName() != null && renameModel.getNewType() != null) { // only rename if passed
                    newSchema = newSchema.add(renameModel.getNewName(), renameModel.getNewType(), element.nullable(), element.metadata());
                }

            } else {
                newSchema = newSchema.add(element);
            }
        }

        sparkDataService.handleChangedColumnsTypes(schemaModelActiveExisting.getSchema(), newSchema, datasetId, branch, transactionId, schemaModelActiveExisting.getSchema(), schemaModelActiveExisting.getCustomSchema());

        schemaModelActiveExisting.setSchema(newSchema);
        schemaModelActiveExisting.setUpdatedBy(userId);
        schemaModelActiveExisting.setUpdatedAt(new Date());

        schemaRepository.save(schemaModelActiveExisting);
        // Remove Redis cache
        clearRelatedCaches(datasetId, branch, cacheRepository);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}

