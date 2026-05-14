package io.orphea.dataset.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.orphea.dataset.library.DTOs.CsvPreprocessingDTO;
import io.orphea.dataset.library.DTOs.PreviewDatasetBySqlDTO;
import io.orphea.dataset.library.models.*;
import io.orphea.dataset.library.repository.DatasetDownloadLogRepository;
import io.orphea.dataset.library.services.*;
import io.orphea.dataset.requests.ChartDataRequest;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.passport.library.Auth;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.service.UserService;
import io.orphea.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dataset", description = "This is a Spark data management service.")
public class DatasetController {
    private final UserService userService;
    private final DatasetService datasetService;
    private final SparkDataService sparkDataService;
    private final ChartDataService chartDataService;
    private final DatasetMappingService datasetMappingService;
    private final DatasetDownloadLogRepository datasetDownloadLogRepository;
    private final SparkResultsService sparkResultsService;
    private final DatasetPreviewQueries datasetPreviewQueries;

    @Operation(summary = "Import branches by datasetId and branch.")
    @GetMapping("/deleteCSV/{datasetId}/{branch}")
    @PreAuthorize(Auth.EDITOR)
    ResponseEntity<Object> deleteCSV(@AuthenticationPrincipal AuthUser authUser,
                                     @PathVariable("datasetId") @Param("id") UUID datasetId,
                                     @PathVariable("branch") String branch) throws Exception {
        UUID userId = authUser.getId();

        datasetService.deleteCsv(datasetId, branch, userId);
        return new ResponseEntity<>("CSV Deleted", HttpStatus.OK);
    }

    @Operation(summary = "Import branches by datasetId and branch.")
    @PostMapping("/import/{datasetId}/{branch}")
    @PreAuthorize(Auth.EDITOR)
    ResponseEntity<Object> uploadCsvDirect(@AuthenticationPrincipal AuthUser authUser,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam("mode") String mode,
                                           @RequestParam("sheetName") String sheetName,
                                           @RequestParam("csvPreprocessing") String csvPreprocessingDTO,
                                           @PathVariable("datasetId") @Param("id") UUID datasetId,
                                           @PathVariable("branch") String branch) throws Exception {
        UUID userId = authUser.getId();
        ObjectMapper objectMapper = new ObjectMapper();
        CsvPreprocessingDTO csvPreprocessing = csvPreprocessingDTO != null && !csvPreprocessingDTO.isEmpty() ? objectMapper.readValue(
                csvPreprocessingDTO, CsvPreprocessingDTO.class) : null;
        ResourceModel folderModel = datasetService.uploadCsv(file, mode, sheetName, datasetId, branch, userId, csvPreprocessing);

        return new ResponseEntity<>(folderModel, HttpStatus.OK);
    }

    @Deprecated
    @Operation(summary = "deleteDatasetFiles files for overwrite by datasetId and branch.")
    @GetMapping("/deleteDatasetFiles/{datasetId}/{branch}")
    @PreAuthorize(Auth.OWNER)
    ResponseEntity<Object> deleteDatasetFiles(@PathVariable("datasetId") @Param("id") UUID datasetId,
                                              @PathVariable("branch") String branch) throws Exception {
        datasetService.deleteForOverwrite(datasetId, branch);

        return new ResponseEntity<>("Dataset files deleted successfully!", HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to filter datasets")
    @PostMapping(path = "/{datasetId}/{branch}/{transactionId}/filtered", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(Auth.VIEWER)
    ResponseEntity<Object> filterDataset(
            @RequestBody FilterModel filterModels,
            @PathVariable("datasetId") @Param("id") UUID datasetId,
            @PathVariable("branch") String branch,
            @PathVariable("transactionId") UUID transactionId,
            @AuthenticationPrincipal AuthUser authUser) throws Exception {

        UUID userId = authUser.getId();

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingService.getCurrentTransaction(datasetId, branch);
        }

        Map<String, Object> response = sparkDataService.getFilteredDataset(filterModels, datasetId, branch, transactionId, userId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "This endpoint can be used to column stats of datasets")
    @PostMapping(path = "/columnStats", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isViewer(#columnStatsModel.datasetId)")
    ResponseEntity<Object> columnStats(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody ColumnStatsModel columnStatsModel) throws Exception {
        UUID userId = authUser.getId();

        String res = datasetService.getColumnStats(columnStatsModel, userId);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint is for frontend to get spark results after socket notification.")
    @GetMapping(path = "/{id}/sparkResults", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getSparkResults(@PathVariable("id") UUID id) {
        SparkResults sparkResults = sparkResultsService.findById(id).orElseThrow();

        sparkResultsService.deleteById(id);

        return new ResponseEntity<>(sparkResults.getResults(), HttpStatus.OK);
    }

    @Operation(summary = "Get unique values for a column")
    @PostMapping("/uniqueColumnValues")
    // TODO: @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> uniqueColumnValues(@RequestBody List<ColumnModel> columnModelList) throws Exception {

        JSONObject result = datasetService.getUniqueColumnValues(columnModelList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get data from postgres.")
    @PostMapping("/postgresData")
    public ResponseEntity<Object> postgresSyncDatasetIdBranchDelete(@RequestBody String query) throws Exception {
        JSONArray json = DatasetService.postgresSyncDelete(query);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @Operation(summary = "Get data using config query")
    @PostMapping("/sqlConfigData")
    @PreAuthorize("isViewer(#query.chartUUID)")
    public ResponseEntity<Object> dataBySqlConfig(@AuthenticationPrincipal AuthUser authUser, @RequestBody ChartDataRequest query) throws Exception {
        User user = userService.getUserById(authUser.getId());

        return new ResponseEntity<>(chartDataService.getDataBySqlConfig(query, user), HttpStatus.OK);

    }

    @Operation(summary = "This can be used to get multiple chart data at once.")
    @PostMapping("/getChartDataByIds")
    public ResponseEntity<Object> getChartDataByIds(@AuthenticationPrincipal AuthUser authUser, @RequestBody ChartDataByIdsRequest
            chartDataByIdsRequest) throws Exception {
        JSONArray chartData = new JSONArray();
        UUID userId = authUser.getId();

        chartDataService.getChartDataByIds(chartDataByIdsRequest, userId, chartData, chartDataByIdsRequest.getUserLocale());

        return new ResponseEntity<>(chartData, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to preview a dataset based on sql.")
    @PostMapping("/previewDatasetBySql")
    public ResponseEntity<Object> preview(@AuthenticationPrincipal AuthUser authUser, @RequestBody PreviewDatasetBySqlDTO
            previewDatasetBySqlDTO) throws Exception {
        UUID userId = authUser.getId();

        Map<String, Object> message = datasetPreviewQueries.previewDatasetBySql(previewDatasetBySqlDTO, userId);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @Operation(summary = "Get history of commands ran on this board")
    @GetMapping("/previewQueriesHistory/{datasetId}")
    public ResponseEntity<Object> previewQueriesHistory(@AuthenticationPrincipal AuthUser authUser, @PathVariable("datasetId") @Param("id") UUID datasetId
    ) throws Exception {
        UUID userId = authUser.getId();

        List<DatasetPreviewQueriesModel> history = datasetPreviewQueries.getPreviewQueriesHistory(datasetId);

        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to Download CSV.")
    @GetMapping("/download/{datasetId}/{branch}/{transactionId}/{format}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Resource> downloadDataset(@AuthenticationPrincipal AuthUser authUser,
                                                    @PathVariable("datasetId") @Param("id") UUID datasetId,
                                                    @PathVariable("branch") String branch,
                                                    @PathVariable("transactionId") UUID transactionId, @PathVariable("format") String format) throws Exception {
        UUID userId = authUser.getId();
        return datasetService.downloadData(datasetId, branch, transactionId, userId, format);
    }

    @Operation(summary = "This can be used to list of Download log.")
    @GetMapping("/downloadLog")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> getDownloadLog() {
        return new ResponseEntity<>(datasetDownloadLogRepository.findAllByOrderByDownloadedAtDesc(), HttpStatus.OK);
    }

    @Operation(summary = "This endpoint is for spark pods to send data")
    @PostMapping(path = "/{buildId}/sparkResults", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> postSparkResults(@Valid @RequestBody SparkResults sparkResults, @PathVariable UUID buildId) throws
            Exception {

        datasetService.sendSparkData(sparkResults, buildId);

        return new ResponseEntity<>("Spark Results stored and socket notified", HttpStatus.OK);

    }

    @Operation(summary = "Get all files for a dataset")
    @GetMapping("/files/{datasetId}/{branch}/{transactionId}")
    @PreAuthorize(Auth.VIEWER)
    ResponseEntity<Object> datasetFiles(@PathVariable("datasetId") @Param("id") UUID datasetId,
                                        @PathVariable("branch") String branch,
                                        @PathVariable("transactionId") UUID transactionId) throws Exception {
        Map<String, Object> files = datasetService.getDatasetFiles(datasetId, branch, transactionId);

        return new ResponseEntity<>(files, HttpStatus.OK);
    }


    @Operation(summary = "Convert String to Date")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/conversion/{type}")
//    @PreAuthorize(Auth.PLATFORM_ADMIN)
    ResponseEntity<Object> convertDate(@PathVariable("datasetId") @Param("id") UUID datasetId,
                                       @PathVariable("branch") String branch,
                                       @PathVariable("transactionId") UUID transactionId, @PathVariable("type") String conversionType) throws Exception {
//        sparkDataService.convertWrapper(datasetId, branch,transactionId,conversionType);
        return null;
    }

}