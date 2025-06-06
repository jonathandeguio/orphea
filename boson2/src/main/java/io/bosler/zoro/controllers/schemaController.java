package io.bosler.zoro.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import io.bosler.bob.library.models.SocketMessage;
import io.bosler.kitab.library.models.BranchModel;
import io.bosler.kitab.library.models.DatasetStatsModel;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.BranchRepository;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.DatasetStatsRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.sharedUtils.BackingFS;
import io.bosler.zoro.library.models.DatasetStatsResponse;
import io.bosler.zoro.library.models.RenameModel;
import io.bosler.zoro.library.models.SchemaApplyModel;
import io.bosler.zoro.library.models.SchemaModel;
import io.bosler.zoro.library.repository.SchemaRepository;
import io.bosler.zoro.library.services.ZoroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static io.bosler.sharedUtils.Redis.*;


@RestController
@RequestMapping("/api/zoro/schema")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Zoro", description = "This is a data management service.")
public class schemaController {

    private final ZoroService zoroService;
    private final DatasetRepository datasetRepository;
    private final DatasetStatsRepository datasetStatsRepository;
    private final FolderRepository folderRepository;
    private final SchemaRepository schemaRepository;
    private final BranchRepository branchRepository;
    private final UserService userService;
    private final AuthzService authzService;

    @Autowired
    SimpMessagingTemplate template;


    @Operation(summary = "This endpoint can be read schema")
    @GetMapping("/{datasetId}/{branch}")
    ResponseEntity<Object> schema(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);

        }

        BranchModel branchModel = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);

        Map<String, Object> result = new HashMap<>();

        if (Objects.equals(branchModel.getType(), "raw")) {

            if (!schemaRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                zoroService.createDBSchemaIfNotExists(datasetId, branch);
            }

            SchemaModel schemaModelActiveExistingUpdated = schemaRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");

            result.put("schema", schemaModelActiveExistingUpdated.getSchema());
            result.put("customSchema", schemaModelActiveExistingUpdated.getCustomSchema());
        } else {

            Dataset<Row> df = zoroService.getSparkDF(datasetId, branch, 0);

            result.put("schema", df.schema());
            result.put("customSchema", "Not Applicable");
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to create / update datasets")
    @GetMapping("/{datasetId}/{branch}/columns")
    ResponseEntity<Object> columns(Principal principal,
                                   @PathVariable("datasetId") UUID datasetId,
                                   @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }


        // Get Redis Cache
        String jsonResults = getCache("columns" + datasetId + branch);
        if (jsonResults != null) {

            ObjectMapper mapper = new ObjectMapper();
            List<Object> colList = mapper.readValue(jsonResults, new TypeReference<List<Object>>(){});

            return new ResponseEntity<>(colList, HttpStatus.OK);
        }


//        Dataset<Row> dfTotal;
//        dfTotal = zoroService.getSparkDF(datasetId, branch, 0);
//
//        Dataset<Row> df = dfTotal.limit(300);

        // TODO : rdd() causing error with time parsing
//        long totalRows = dfTotal.rdd().count(); // gives number of rows
//        long totalColumns = dfTotal.columns().length; // gives number of rows
//        long displayRows = df.rdd().count(); // gives number of rows
//        long displayColumns = df.columns().length; // gives number of rows

//        long totalRows = 0; // gives number of rows
//        long totalColumns = 0; // gives number of rows
//        long displayRows = 0; // gives number of rows
//        long displayColumns = 0; // gives number of rows

//        Map<String, Object> stats = new HashMap<>();
//
//        stats.put("displayRows", displayRows);
//        stats.put("displayColumns", displayColumns);


        // columns dict
        List<Object> colList = new ArrayList<>();

        BranchModel branchModel = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);

        if (Objects.equals(branchModel.getType(), "raw")) {

            if (!schemaRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                zoroService.createDBSchemaIfNotExists(datasetId, branch);
            }

            SchemaModel schemaModelActiveExistingUpdated = schemaRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");

            for (StructField element : schemaModelActiveExistingUpdated.getSchema().fields()) {
                Map<String, Object> cols = new HashMap<>();
                cols.put("headerName", element.name());
                cols.put("field", element.name());
                cols.put("type", element.dataType().typeName());
                colList.add(cols);
            }

        } else {
            Dataset<Row> df = zoroService.getSparkDF(datasetId, branch, 0);
            for (StructField element : df.schema().fields()) {
                Map<String, Object> cols = new HashMap<>();
                cols.put("headerName", element.name());
                cols.put("field", element.name());
                cols.put("type", element.dataType().typeName());
                colList.add(cols);
            }
        }

        // Set Redis Cache
        ObjectMapper objectMapper = new ObjectMapper();
        String respData = objectMapper.writeValueAsString(colList);
        setCache("columns" + datasetId + branch, respData, null);

        return new ResponseEntity<>(colList, HttpStatus.OK);

    }

    @Operation(summary = "This endpoint can be used to calculate datasets stats")
    @GetMapping("/{datasetId}/{branch}/calculateStats")
    ResponseEntity<Object> calculateStats(Principal principal,
                                          @PathVariable("datasetId") UUID datasetId,
                                          @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }


        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");

        template.convertAndSend("/topic/statsCalculation/" + datasetId + "/" + branch, textMessage);

        zoroService.statsCalculation(userId, datasetId, branch);

        return new ResponseEntity<>("Stats Calculation in progress", HttpStatus.OK);
    }


    @Operation(summary = "This endpoint can be used to get dataset stats")
    @GetMapping("/{datasetId}/{branch}/stats")
    ResponseEntity<Object> stats(Principal principal,
                                 @PathVariable("datasetId") UUID datasetId,
                                 @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }


        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        DatasetStatsResponse datasetStatsResponse = new DatasetStatsResponse();

        FolderModel datasetInfo = folderRepository.getById(datasetId);

        datasetStatsResponse.setFolderModel(datasetInfo);

        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(datasetId, branch);

            datasetStatsResponse.setDatasetStatsModel(datasetStatsModel);

            return new ResponseEntity<>(datasetStatsResponse, HttpStatus.OK);
        } else {

            Dataset<Row> dfTotal = zoroService.getSparkDF(datasetId, branch, 0);

//            long totalRows = dfTotal.count(); // gives number of rows
            long totalColumns = dfTotal.columns().length; // gives number of rows

            List<Map<String, Object>> listOfFiles = BackingFS.getListOfFiles(datasetId, branch);

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
    @PostMapping("/{datasetId}/{branch}/apply")
    ResponseEntity<Object> applySchema(Principal principal, @PathVariable("datasetId") UUID datasetId, @RequestBody SchemaApplyModel complexSchema, @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

//        if (!authzService.isEditor(userId, datasetId)) {
//            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
//        }

        // TODO : validations

        SchemaModel schemaModelActiveExisting = schemaRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");

        // TODO : versioning...
//        if (schemaModelActiveExisting != null) {
//            schemaModelActiveExisting.setStatus("disabled");
//            schemaModelActiveExisting.setUpdatedBy(userId);
//            schemaModelActiveExisting.setUpdatedAt(new Date());
//        }

        schemaModelActiveExisting.setUpdatedBy(userId);
        schemaModelActiveExisting.setUpdatedAt(new Date());


        if (
                !(schemaModelActiveExisting.getCustomSchema().getId().equals(complexSchema.getCustomSchema().getId()))
                        || !(schemaModelActiveExisting.getCustomSchema().getEscapeCharacter().equals(complexSchema.getCustomSchema().getEscapeCharacter()))
                        || !(schemaModelActiveExisting.getCustomSchema().getFieldDelimiter().equals(complexSchema.getCustomSchema().getFieldDelimiter()))
                        || !(schemaModelActiveExisting.getCustomSchema().getLineDelimiter().equals(complexSchema.getCustomSchema().getLineDelimiter()))
        ) {
            schemaModelActiveExisting.setCustomSchema(complexSchema.getCustomSchema());

            StructType schema = zoroService.getSchema(datasetId, branch, complexSchema.getCustomSchema());
            schemaModelActiveExisting.setSchema(schema);

        } else {
            schemaModelActiveExisting.setCustomSchema(complexSchema.getCustomSchema());
            schemaModelActiveExisting.setSchema(complexSchema.getSchema());
        }

        // Remove Redis cache
        deleteCache("dataset" + datasetId + branch);
        deleteCache("columns" + datasetId + branch);
        deleteCacheWithWildCard("sparkResults" + datasetId + branch + "*");
        deleteCacheWithWildCard("chartData" + datasetId + branch + "*");

        schemaRepository.save(schemaModelActiveExisting);

        Map<String, Object> result = new HashMap<>();

        result.put("schema", schemaModelActiveExisting.getSchema());
        result.put("customSchema", schemaModelActiveExisting.getCustomSchema());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to rename column names")
    @PostMapping("/{datasetId}/{branch}/rename")
    ResponseEntity<Object> rename(Principal principal, @PathVariable("datasetId") UUID datasetId, @RequestBody RenameModel renameModel, @PathVariable("branch") String branch) throws IOException {

        UUID userId = userService.getUser(principal.getName()).id;

//        if (!authzService.isEditor(userId, datasetId)) {
//            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
//        }

        StructType oldSchema = new StructType(), newSchema = new StructType();
        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            System.out.println("No dataset found in catalog for " + datasetId);

        }
        SchemaModel schemaModelActiveExisting = schemaRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");

        // TODO : versioning
//        if (schemaModelActiveExisting != null) {
//            schemaModelActiveExisting.setStatus("disabled");
//            schemaModelActiveExisting.setUpdatedBy(userId);
//            schemaModelActiveExisting.setUpdatedAt(new Date());
//
//            oldSchema = schemaModelActiveExisting.getSchema();
//        }
        oldSchema = schemaModelActiveExisting.getSchema();

        for (StructField element : oldSchema.fields()) {
            if (element.name().equals(renameModel.getColumnName())) {
                if (renameModel.getNewName() != null && renameModel.getNewType() != null) { // only rename if passed
                    newSchema = newSchema.add(renameModel.getNewName(), renameModel.getNewType(), element.nullable(), element.metadata());
                }

            } else {
                newSchema = newSchema.add(element);
            }
        }

        // TODO : validations
        // TODO : error handling

//        System.out.println("printing schema");


        schemaModelActiveExisting.setSchema(newSchema);  // Final update into DB
        schemaModelActiveExisting.setUpdatedBy(userId);
        schemaModelActiveExisting.setUpdatedAt(new Date());

        schemaRepository.save(schemaModelActiveExisting);

        return new ResponseEntity<>("Rename Successful", HttpStatus.OK);
    }

}

