package io.movetodata.dataset.library.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.BobEnums.FunnelStatus;
import io.movetodata.build.library.enums.WriteModeEnum;
import io.movetodata.build.library.models.SocketMessage;
import io.movetodata.build.library.services.BuildLogService;
import io.movetodata.build.library.services.K8Service;
import io.movetodata.dataset.library.DTOs.CsvPreprocessingDTO;
import io.movetodata.dataset.library.DTOs.DatasetFiltersDTO;
import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.models.*;
import io.movetodata.dataset.library.repository.DatasetDownloadLogRepository;
import io.movetodata.dataset.library.repository.DatasetMappingRepository;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.dataset.requests.ChartDataRequest;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import io.movetodata.kepler.library.models.ChartConfigModel;
import io.movetodata.kepler.library.models.DatasetFilterModel;
import io.movetodata.kepler.library.models.SeriesModel;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.models.DatasetStatsModel;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.DatasetStatsRepository;
import io.movetodata.kitab.library.services.BranchService;
import io.movetodata.kitab.library.services.DatasetWritingTransactionService;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.platform.library.models.PlatformConfig;
import io.movetodata.platform.library.models.SparkConfigModel;
import io.movetodata.platform.library.repository.CacheRepository;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import io.movetodata.platform.library.repository.SparkConfigRepository;
import io.movetodata.sharedutils.BackingFS;
import io.movetodata.sharedutils.DeletionInBackingFS;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import io.movetodata.sharedutils.Utils;
import io.movetodata.synchro.library.repository.PostgresSyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.functions;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

import static io.movetodata.sharedutils.Redis.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetService {
    private final CacheRepository cacheRepository;
    private final DatasetStatsRepository datasetStatsRepository;
    private final SchemaRepository schemaRepository;
    private final DatasetRepository datasetRepository;
    private final PlatformConfigRepository platformConfigRepository;
    private final BranchService branchService;
    private final SimpMessagingTemplate template;
    private final PostgresSyncRepository postgresSyncRepository;
    private final DatasetDownloadLogRepository datasetDownloadLogRepository;
    private final BackingFS backingFS;
    private final SparkResultsService sparkResultsService;
    private final DatasetMappingRepository datasetMappingRepository;
    private final DeletionInBackingFS deletionInBackingFS;
    private final AsyncSparkService asyncSparkService;
    private final ResourceService resourceService;
    private final SparkConfigRepository sparkConfigRepository;
    private final K8Service k8Service;
    private final BuildLogService buildLogService;
    private final DatasetWritingTransactionService datasetWritingTransactionService;
    private final DatasetMappingService datasetMappingService;
    private final SparkDataService sparkDataService;


    public static JSONArray convertResultSetIntoJSON(ResultSet resultSet) throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < total_rows; i++) {
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), resultSet.getObject(i + 1));
            }
            jsonArray.add(obj);
        }
        return jsonArray;
    }

    public static ChartDataRequest converterOne(UUID chartUUID, ChartConfigModel
            chartConfigModel, List<DatasetFiltersDTO> filters, Boolean saveInCache, UUID transactionId) throws ParseException {
        ChartDataRequest request = new ChartDataRequest();

        request.copyNonNullProperties(chartConfigModel);

        request.setChartUUID(chartUUID);
        request.setSaveInCache(saveInCache);
        request.setTransactionId(transactionId);
        request.setFetchCachedData(true);

        List<ChartSeriesRequest> seriesModelList = new ArrayList<>();
        for (SeriesModel seriesModel : chartConfigModel.getSeries()) {
            ChartSeriesRequest s = new ChartSeriesRequest();
            s.setId(seriesModel.getId());
            s.setSeriesId(seriesModel.getSeriesId());
            s.setSeriesName(seriesModel.getSeriesName());
            s.setColumnName(seriesModel.getColumnName());
            s.setAggregate(seriesModel.getAggregate());
            s.setSort(seriesModel.getSort());
            s.setSeriesIndex(seriesModel.getSeriesIndex());
            s.setSeriesType(seriesModel.getSeriesType());
            s.setReversed(seriesModel.getReversed());

//            ArrayList<String> groupBy = new ArrayList<>(seriesModel.getGroupBy());
//            s.setGroupBy(groupBy);
            seriesModelList.add(s);
        }
        request.setSeries(seriesModelList);

        List<DatasetFiltersDTO> filterModelList = new ArrayList<>();
        for (DatasetFilterModel filter : chartConfigModel.getFilter()) {
            filterModelList.add(Utils.convertDatasetFilterModelToDatasetFilterDTO(filter));
        }

        for (DatasetFiltersDTO filter : filters) {
            request.setFetchCachedData(false);
            filterModelList.add(filter);
        }

        request.setFilter(filterModelList);
        return request;
    }

    @NotNull
    public static JSONArray postgresSyncDelete(String query) throws Exception {
        JSONArray json;
        try (Connection connection = DriverManager
                .getConnection("jdbc:postgresql://" + System.getenv("DB_HOST") + ":5432/kepler",
                        System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
             Statement stmt = connection.createStatement()) {

            stmt.executeQuery(query);

            ResultSet result = stmt.executeQuery(query);
            json = DatasetService.convertResultSetIntoJSON(result);
        }
        return json;
    }

    public static boolean hasSchemaChanged(SchemaModel existing, SchemaApplyModel complex) {
        CustomSchemaModel existingCustom = existing.getCustomSchema();
        CustomSchemaModel complexCustom = complex.getCustomSchema();

        return !(existingCustom.getId().equals(complexCustom.getId()) &&
                existingCustom.getEscapeCharacter().equals(complexCustom.getEscapeCharacter()) &&
                existingCustom.getFieldDelimiter().equals(complexCustom.getFieldDelimiter()) &&
                existingCustom.getLineDelimiter().equals(complexCustom.getLineDelimiter()) &&
                existingCustom.getEncoding().equals(complexCustom.getEncoding()));
    }

    //    @Async
    public void statsCalculation(UUID userId, UUID datasetId, String branch, UUID transactionId) throws Exception {
        Dataset<Row> dfTotal = sparkDataService.getSparkDF(datasetId, branch, transactionId, -1);

        long totalRows = dfTotal.count(); // gives number of rows
        long totalColumns = dfTotal.columns().length; // gives number of rows

        List<Map<String, Object>> listOfFiles = BackingFS.getListOfFiles(datasetId, branch, transactionId);

        int numFiles = listOfFiles.size();

        long dfSize = 0;

        for (Map<String, Object> files : listOfFiles) {
            long sizeInBytes = (long) files.get("sizeInBytes");
            dfSize += sizeInBytes;
        }

        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {

            DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(datasetId, branch);


            datasetStatsModel.setRows(totalRows);
            datasetStatsModel.setColumns(totalColumns);

            datasetStatsModel.setFiles(numFiles);
            datasetStatsModel.setSize(dfSize);

            datasetStatsModel.setUpdatedAt(new Date());
            datasetStatsModel.setUpdatedBy(userId);

            DatasetStatsModel datasetStatsModelSaved = datasetStatsRepository.save(datasetStatsModel);

            SocketMessage textMessage = new SocketMessage();
            textMessage.setMessage("success");

            template.convertAndSend("/topic/statsCalculation/" + datasetId + "/" + branch, textMessage);

//            return new ResponseEntity<>(datasetStatsModelSaved, HttpStatus.OK);
        } else {

            DatasetStatsModel datasetStatsModel = new DatasetStatsModel();

            datasetStatsModel.setDatasetId(datasetId);
            datasetStatsModel.setBranch(branch);

            datasetStatsModel.setRows(totalRows);
            datasetStatsModel.setColumns(totalColumns);

            datasetStatsModel.setFiles(numFiles);
            datasetStatsModel.setSize(dfSize);

            datasetStatsModel.setCreatedAt(new Date());
            datasetStatsModel.setCreatedBy(userId);

            DatasetStatsModel datasetStatsModelSaved = datasetStatsRepository.save(datasetStatsModel);

            SocketMessage textMessage = new SocketMessage();
            textMessage.setMessage("success");

            template.convertAndSend("/topic/statsCalculation/" + datasetId + "/" + branch, textMessage);

//            return new ResponseEntity<>(datasetStatsModelSaved, HttpStatus.OK);
        }
    }

    public void sendSparkData(SparkResults sparkResults, UUID buildId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String respData = objectMapper.writeValueAsString(sparkResults.getResults());

        setCache("sparkResults" + sparkResults.getDatasetId() + sparkResults.getBranch() + sparkResults.getColumnName(), respData, cacheRepository);

        sparkResultsService.save(sparkResults);
        // Finishing builds
        buildLogService.finishBuild(buildId);
        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("success");

        template.convertAndSend("/topic/sparkResults/" + sparkResults.getId(), textMessage);
    }

    @NotNull
    public Map<String, Object> getDatasetFiles(UUID datasetId, String branch, UUID transactionId) throws Exception {
        // check if the dataset exists in catalog
        if (!datasetRepository.existsById(datasetId)) {
            throw new NoSuchElementException("No dataset found in catalog for " + datasetId);
        }

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(datasetId, branch)).getCurrentTransaction();
        }

        List<Map<String, Object>> rows = BackingFS.getListOfFiles(datasetId, branch, transactionId);

        Map<String, Object> files = new HashMap<>();
        List<Map<String, Object>> columns = new ArrayList<>();

        Map<String, Object> cols = new HashMap<>();

        cols.put("title", "File Name");
        cols.put("dataIndex", "path");
        cols.put("key", "path");
        columns.add(cols);

        Map<String, Object> size = new HashMap<>();

        size.put("title", "Size");
        size.put("dataIndex", "size");
        size.put("key", "size");
        columns.add(size);

        Map<String, Object> lastModified = new HashMap<>();

        lastModified.put("title", "Last Modified");
        lastModified.put("dataIndex", "lastModified");
        lastModified.put("key", "lastModified");
        columns.add(lastModified);

        files.put("rows", rows);
        files.put("columns", columns);
        return files;
    }

    public String getColumnStats(ColumnStatsModel columnStatsModel, UUID userId) throws Exception {
        if (!datasetRepository.existsById(columnStatsModel.getDatasetId())) { // check if the dataset exists in catalog
            throw new NoSuchElementException("No dataset found in catalog for " + columnStatsModel.getDatasetId());
        }

        String res = getCache("sparkResults" + columnStatsModel.getDatasetId() + columnStatsModel.getBranch() + columnStatsModel.getColumn(), cacheRepository);
        if (res == null) {
            UUID resultsId = UUID.randomUUID();

            SparkConfigModel sparkConfigModel = sparkConfigRepository.findByConfig("platform");
            log.info(">>>> Current mode is : " + sparkConfigModel.getColumnStats());
            if (Objects.equals(sparkConfigModel.getColumnStats(), "kubernetes")) {
                BranchModel branchModel = branchService.getBranchModel(columnStatsModel.getDatasetId(), columnStatsModel.getBranch());
                SchemaModel schemaModel = sparkDataService.findByDatasetIdAndBranchAndTransactionIdAndStatus(columnStatsModel.getDatasetId(), columnStatsModel.getBranch(), columnStatsModel.getTransactionId());
                HashMap<String, String> envVars = k8Service.settingEnvVarsForColumnStats(columnStatsModel.getDatasetId(), columnStatsModel.getBranch(), columnStatsModel.getTransactionId(), columnStatsModel.getColumn(), userId, resultsId, schemaModel.getCustomSchema().getEncoding(), branchModel.getType());
                asyncSparkService.asyncRunSparkColumnStatsJobWithKubernetes(envVars, userId);
            } else {
                Thread backgroundThread = new Thread(() -> {
                    try {
                        Dataset<Row> dfTotal = sparkDataService.getSparkDF(columnStatsModel.getDatasetId(), columnStatsModel.getBranch(), columnStatsModel.getTransactionId(), -1);

                        asyncSparkService.asyncCalculateStatistics(dfTotal, columnStatsModel, resultsId);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
                backgroundThread.start();
            }
            res = resultsId.toString();
        }
        return res;
    }


    public void deleteCsv(UUID datasetId, String branch, UUID userId) throws Exception {
        datasetWritingTransactionService.verifyAndDeleteTransaction(datasetId, branch);

        // Remove Redis cache
        clearRelatedCaches(datasetId, branch, cacheRepository);

        deletionInBackingFS.deleteDatasetFiles("dataset", datasetId, branch);

        // after successful deletion of transactions deleting dataset Mapping
        datasetMappingRepository.deleteById(new DatasetMappingKey(datasetId, branch));

        Optional<BranchModel> branchModelOptional = branchService.findBranchModelByDatasetIdAndBranch(datasetId, branch);

        DatasetModel datasetModel = datasetRepository.getReferenceById(datasetId);

        Set<BranchModel> branches = datasetModel.getBranches();

        if (branchModelOptional.isPresent()) {
            BranchModel branchModel = branchModelOptional.get();
            branches.remove(branchModel);
            datasetModel.setBranches(branches);
            branchService.delete(branchModel);
        }

        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            List<DatasetStatsModel> datasetStatsModel = datasetStatsRepository.findAllByDatasetIdAndBranch(datasetId, branch);
            datasetStatsRepository.deleteAll(datasetStatsModel);
        }
        // deleting schemas
        schemaRepository.deleteAll(schemaRepository.findByDatasetIdAndBranch(datasetId, branch));

        // deleting postgres synchro
        postgresSyncRepository.deleteAll(postgresSyncRepository.findAllByDatasetIdAndBranch(datasetId, branch));

        // Disabling auto stats calculations
        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(datasetId, branch);
            datasetStatsRepository.delete(datasetStatsModel);
        }

//            TODO
//          deleting schedules
//          deleting charts associated with that dataset

        // Update kitab dataset also
        datasetRepository.save(datasetModel);

        ResourceModel resourceModel = resourceService.getResourceModel(datasetId);
        resourceModel.setUpdatedAt(new Date());
        resourceModel.setUpdatedBy(userId);
        resourceService.save(resourceModel);
    }

    public ResourceModel uploadCsv(MultipartFile file, String mode, String sheetName, UUID datasetId, String branch, UUID userId, CsvPreprocessingDTO csvPreprocessingDTO) throws Exception {
        ResourceSubtype fileType = BackingFS.getFileType(file);
        List<MultipartFile> files = new ArrayList<>();
        files.add(file);
        UUID buildId = UUID.randomUUID();
        buildLogService.initialBuildLogSetupWithSockets(buildId, datasetId, userId, BuildTrigger.UPLOAD, BuildLaunchedBy.UPLOAD, null, branch);

        try {
            UUID newTransactionId = datasetMappingService.createTransaction(datasetId, branch, userId, BuildLaunchedBy.UPLOAD, buildId, WriteModeEnum.SNAPSHOT);
            backingFS.UploadCSV(files, datasetId, branch, mode, ResourceSubtype.RAWDATASET, fileType, userId, buildId, newTransactionId, BuildTrigger.UPLOAD, sheetName, csvPreprocessingDTO);
            return resourceService.findById(datasetId).get();
        } catch (Exception err) {
            log.error("uploadCsv failed - full stack trace:", err);
            buildLogService.createBuildLogEntryWithDebug("Build failed", err.getMessage(), BuildStage.FINISHED, FunnelStatus.FAILED, buildId, null);
            return null;
        }
    }

    public void deleteForOverwrite(UUID datasetId, String branch) throws Exception {
        branchService.findBranchModelByDatasetIdAndBranch(datasetId, branch).orElseThrow(() -> new NoSuchElementException("No such Dataset."));

        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            List<DatasetStatsModel> datasetStatsModel = datasetStatsRepository.findAllByDatasetIdAndBranch(datasetId, branch);
            datasetStatsRepository.deleteAll(datasetStatsModel);
        }

        // Remove Redis cache
        clearRelatedCaches(datasetId, branch, cacheRepository);

        deletionInBackingFS.deleteDatasetFiles("dataset", datasetId, branch);
    }

    @NotNull
    public JSONObject getUniqueColumnValues(List<ColumnModel> columnModelList) throws Exception {
        JSONObject result = new JSONObject();
        // Check if Dataset Exists by ID and branch
        for (ColumnModel columnModel : columnModelList) {
            ArrayList<Object> res = new ArrayList<>();
            if (!resourceService.existsById(columnModel.getDatasetID())) {
                throw new NoSuchElementException("Dataset with Id " + columnModel.getDatasetID().toString() + " does not exist");
            }
            UUID transactionId = columnModel.getTransactionId();
            if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
                transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(columnModel.getDatasetID(), columnModel.getBranch())).getCurrentTransaction();
            }
            Dataset<Row> sparkData = sparkDataService.getSparkDF(columnModel.getDatasetID(), columnModel.getBranch(), transactionId, -1);

            switch (columnModel.getType()) {
                case "value": {
                    Dataset<Row> groupedDataset = sparkData.select(columnModel.getColumn()).distinct();
                    groupedDataset = groupedDataset.filter(groupedDataset.col(columnModel.getColumn()).isNotNull());

                    for (Row r : groupedDataset.collectAsList()) {
                        res.add(r.getAs(columnModel.getColumn()));
                    }
                    break;
                }
                case "range": {
                    Object minVal = sparkData.agg(functions.min(columnModel.getColumn())).first().get(0);
                    Object maxVal = sparkData.agg(functions.max(columnModel.getColumn())).first().get(0);

                    res.add(minVal);
                    res.add(maxVal);
                    break;
                }
                case "classic": {
                    if (columnModel.getColumnType().equals("timestamp")) {
                        Object minVal = sparkData.agg(functions.min(columnModel.getColumn())).first().get(0);
                        Object maxVal = sparkData.agg(functions.max(columnModel.getColumn())).first().get(0);

                        res.add(minVal);
                        res.add(maxVal);
                    } else {
                        Dataset<Row> groupedDataset = sparkData.select(columnModel.getColumn()).distinct();

                        for (Row r : groupedDataset.collectAsList()) {
                            res.add(r.getAs(columnModel.getColumn()));
                        }
                    }

                    break;
                }
            }

            if (columnModel.getFilterId() == null) {
                result.put(columnModel.getDatasetID() + columnModel.getColumn() + columnModel.getBranch() + columnModel.getType(), res);
            } else {
                result.put(columnModel.getFilterId(), res);
            }
        }
        return result;
    }


    public MultipartFile FileTypeDetection(MultipartFile file) throws Exception {
        ResourceSubtype fileType = BackingFS.getFileType(file);
        IOUtils.setByteArrayMaxOverride(1000_000_000);

        if (!fileType.equals(ResourceSubtype.CSV) && !fileType.equals(ResourceSubtype.XLS) && !fileType.equals(ResourceSubtype.PARQUET)) {
            return file;
        }

//        if (fileType.equals(ResourceSubtype.XLS)) {
//            try {
//                String sheetToConvert = sheetName.equalsIgnoreCase("first")
//                        ? excelService.getExcelSheetNames(file).stream().findFirst().orElse(sheetName)
//                        : sheetName;
//
//                MultipartFile convertedFile = excelService.convertExcelToCsv(file, sheetToConvert);
//                if (convertedFile != null) {
//                    return convertedFile;
//                }
//            } catch (Exception e) {
//                System.out.println("Error converting Excel file to CSV: " + e);
//                return file; // Return the original file if conversion fails
//            }
//        }

        return file;
    }

    @NotNull
    public ResponseEntity<Resource> downloadData(UUID datasetId, String branch, UUID transactionId, UUID userId, String format) throws Exception {

        PlatformConfig platformConfig = platformConfigRepository.findByName("platformConfig").orElseThrow();
        if (!platformConfig.isDownload()) {
            throw new UnsupportedOperationException("Dataset Download is not allowed");
        }
        Integer downloadRowLimit = platformConfig.getRowLimit();
        Long downloadSizeLimit = platformConfig.getSizeLimit();
        // Log the download action
        DatasetDownloadLog downloadLog = new DatasetDownloadLog();
        downloadLog.setDownloadedBy(userId);
        downloadLog.setDownloadedAt(new Date());
        downloadLog.setDatasetId(datasetId);
        downloadLog.setBranch(branch);
        downloadLog.setNumberOfRows(downloadRowLimit);
        downloadLog.setSize(downloadSizeLimit);
        datasetDownloadLogRepository.save(downloadLog);

        Dataset<Row> dataset = sparkDataService.getSparkDF(datasetId, branch, transactionId, downloadRowLimit);
        if (dataset.isEmpty()) {
            throw new UnsupportedOperationException("Dataset " + datasetId + " is empty.");
        }
        // Determine folder path and format-specific subdirectory
        String folderPath = System.getenv("MOVETODATA_MOUNT_PATH") + "/tmp/" + format + "/" + datasetId;
        File datasetFolder = new File(folderPath);

        // Clean up old files if they exist
        if (datasetFolder.exists() && datasetFolder.isDirectory()) {
            File[] listOfFiles = datasetFolder.listFiles();
            for (File delFile : listOfFiles) {
                delFile.delete();
            }
            datasetFolder.delete();
        }
        datasetFolder.mkdirs();

        //writes with dataset content in the file (csv, parquet)
        //NOTE:- NEED TO CHECK BECAUSE THIS DOES NOT WRITE IN THE FILE RETURNS EMPTY FILE
        dataset.coalesce(1).write()
                .format(format)  // Use the format variable to switch between csv and parquet
                .option("header", true)
                .mode(SaveMode.Overwrite)
                .save(folderPath);

        File tempFile1 = new File(folderPath);

        // Check if the file exists
        if (tempFile1.isDirectory()) {
            File[] listOfFiles = tempFile1.listFiles();

            ResponseEntity<Resource> response = new ResponseEntity("File NOT FOUND!", HttpStatus.NOT_FOUND);

            assert listOfFiles != null;
            for (File file : listOfFiles) {
                Pattern pattern = Pattern.compile("^part", Pattern.CASE_INSENSITIVE);

                if (pattern.matcher(file.getName()).find()) {
                    if (file.length() > downloadSizeLimit) {
                        throw new UnauthorizedException("File Too Big, Download not allowed!");
                    }

                    InputStream stream = new FileInputStream(file);
                    InputStreamResource streamResource = new InputStreamResource(stream);

                    // Change content type and filename extension based on the format
                    String fileName = resourceService.getResourceModel(datasetId)
                            .getName()
                            .split("\\.")[0] + (format.equals("csv") ? ".csv" : ".parquet");
                    String contentType = format.equals("csv") ? "text/csv" : "application/octet-stream";

                    response = ResponseEntity
                            .ok()
                            .header("Content-Disposition", "attachment")
                            .header("filename", fileName)
                            .header("Access-Control-Expose-Headers", "filename")
                            .contentType(MediaType.parseMediaType(contentType))
                            .contentLength(file.length())
                            .body(streamResource);
                }
            }
            for (File file : listOfFiles) {
                file.delete();
            }
            tempFile1.delete();
            return response;
        } else {
            throw new ResourceNotFoundException("No Such File! Please contact admin.");
        }
    }
}