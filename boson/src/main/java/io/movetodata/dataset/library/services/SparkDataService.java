package io.movetodata.dataset.library.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.movetodata.build.library.dto.SourceDataset;
import io.movetodata.connect.library.enums.SourceAuthTypeEnum;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.connect.library.models.Link;
import io.movetodata.connect.library.models.Source;
import io.movetodata.connect.library.repository.LinkRepository;
import io.movetodata.connect.library.services.DatabaseSourceConfigService;
import io.movetodata.connect.library.services.JDBCService;
import io.movetodata.connect.library.services.SourceService;
import io.movetodata.dataset.helper.SparkUDF;
import io.movetodata.dataset.library.DTOs.*;
import io.movetodata.dataset.library.models.CustomSchemaModel;
import io.movetodata.dataset.library.models.FilterModel;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.dataset.library.repository.DatasetMappingTransactionRepository;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.dataset.library.services.Spark.CsvPreprocessingService;
import io.movetodata.kepler.enums.LogicalOperator;
import io.movetodata.kepler.library.models.DatasetFilterModel;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.services.BranchService;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedutils.Exceptions.EnvConfigurationException;
import io.movetodata.sharedutils.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static io.movetodata.sharedutils.BackFsFileUtils.getResourcePath;
import static io.movetodata.sharedutils.Utils.isBase64;
import static org.apache.spark.sql.functions.*;

@Slf4j
@Service
@AllArgsConstructor
public class SparkDataService {
    private final JDBCService jdbcService;
    private final SchemaRepository schemaRepository;
    private final DatasetRepository datasetRepository;
    private final DatabaseSourceConfigService databaseSourceConfigService;
    private final SourceService sourceService;
    private final BranchService branchService;
    private final LinkRepository linkRepository;
    private final UserService userService;
    private final DatasetMappingService datasetMappingService;
    private final SparkSession sparkSession;
    private final CsvPreprocessingService csvPreprocessingService;
    private final DatasetMappingTransactionRepository datasetMappingTransactionRepository;

    @NotNull
    private static HashMap<String, String> getDateFormatMap() {
        HashMap<String, String> dateFormatMap = new HashMap<>();
        dateFormatMap.putIfAbsent("year", "yyyy");
        dateFormatMap.putIfAbsent("quarter", "yyyy 'Q'q");
        dateFormatMap.putIfAbsent("month", "MMM yyyy");
        dateFormatMap.putIfAbsent("week", "yyyy 'W'w");
        dateFormatMap.putIfAbsent("day", "yyyy 'W'w E");
        dateFormatMap.putIfAbsent("date", "MMM dd, yyyy");
        dateFormatMap.putIfAbsent("hour", "MMM dd, yyyy, HH:00");
        dateFormatMap.putIfAbsent("minute", "MMM dd, yyyy HH:mm");
        dateFormatMap.putIfAbsent("second", "MMM dd, yyyy HH:mm:ss");
        return dateFormatMap;
    }

    @Transactional
    public Dataset<Row> getFilteredSparkDataframe(UUID datasetId, String branch, String transactionId, String timeGrain, List<DatasetFiltersDTO> filterList, int limit, String locale) throws Exception {
        final HashMap<String, String> dateFormatMap = getDateFormatMap();

//      TODO: ADD ROW LIMIT HERE WHEN FETCHING THE SPARK DATA FRAME
        SparkUDF myDateFormat = new SparkUDF("myDateFormat", udf(
                (java.sql.Timestamp date) -> {
                    if (date == null) return "null";
                    String format = dateFormatMap.getOrDefault(timeGrain, "MMM dd, yyyy");

                    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(format, new Locale(locale));
                    return date.toLocalDateTime().format(DATE_FORMAT);

                }, DataTypes.StringType
        ));
        SparkUDF myDateFormat2 = new SparkUDF("myDateFormat2", udf(
                (java.sql.Date date) -> {
                    if (date == null) return "null";
                    String format = dateFormatMap.getOrDefault(timeGrain, "MMM dd, yyyy");

                    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(format, new Locale(locale));
                    return date.toLocalDate().atTime(LocalTime.MIDNIGHT).format(DATE_FORMAT);

                }, DataTypes.StringType
        ));


        Dataset<Row> sparkData = getSparkDF(datasetId, branch, UUID.fromString(transactionId), limit, myDateFormat, myDateFormat2);

        if (filterList != null) {
            for (DatasetFiltersDTO filter : filterList) {
                String columnName = filter.getColumnName();
                LogicalOperator logicalOperator = filter.getLogicalOperator();

                boolean flag = false;
                for (StructField field : sparkData.schema().fields()) {
                    if (field.name().equals(columnName)) {
                        flag = true;
                    }
                }

                if (!flag) {
                    return sparkData.limit(0);
                }

                Column column = sparkData.col(columnName);
                Column prevCond = null;

                int i = 0;
                if (filter.getFilters() != null) {
                    for (FilterDTO f : filter.getFilters()) {
                        String value = f.getValue();
                        String columnType = filter.getColumnType();
                        String operator = f.getOperator();

                        Column newCol = null;

                        Column columnBasedOnType = column;

//                        if(columnType.equals("timestamp") || columnType.equals("date")) {
//                        if (columnName.equalsIgnoreCase("Transaction_date")) {
//                            String grain = "year";
//
//                            columnBasedOnType = date_trunc(grain, column);
//                            System.out.println("--------------------------------------------------");
//                            for (Row r : sparkData.withColumn("truncated", columnBasedOnType).collectAsList()) {
//                                System.out.println(r.json());
//                                break;
//                            }
//                            System.out.println("--------------------------------------------------");
//                            System.out.println(value);
//                        }

                        switch (operator) {
                            case "equal":
                                newCol = columnBasedOnType.equalTo(value);
                                break;
                            case "notEqual":
                                newCol = columnBasedOnType.notEqual(value);
                                break;
                            case "lessThan":
                                newCol = columnBasedOnType.lt(value);
                                break;
                            case "lessThanEqual":
                                newCol = columnBasedOnType.leq(value);
                                break;
                            case "greaterThan":
                                newCol = columnBasedOnType.gt(value);
                                break;
                            case "greaterThanEqual":
                                newCol = columnBasedOnType.geq(value);
                                break;
                            case "in": {
                                ObjectMapper objectMapper = new ObjectMapper();
                                Object[] valArr = objectMapper.readValue(value, Object[].class);

                                newCol = columnBasedOnType.isin(valArr);
                                break;
                            }
                            case "notIn": {
                                ObjectMapper objectMapper = new ObjectMapper();
                                Object[] valArr = objectMapper.readValue(value, Object[].class);

                                newCol = not(columnBasedOnType.isin(valArr));
                                break;
                            }
                            case "like":
                                newCol = columnBasedOnType.like(value);
                                break;
                            case "notLike":
                                newCol = not(columnBasedOnType.like(value));
                                break;
                            case "doesNotContains":
                                newCol = not(columnBasedOnType.contains(value));
                                break;
                            case "contains":
                                newCol = columnBasedOnType.contains(value);
                                break;
                            case "exists":
                                newCol = columnBasedOnType.isNotNull();
                                break;
                            case "doesNotExist":
                                newCol = columnBasedOnType.isNull();
                                break;
                        }


                        if (i++ == 0) {
                            prevCond = logicalOperatorHelper(logicalOperator, prevCond, newCol);
                        } else {
                            prevCond = logicalOperatorHelper(logicalOperator, prevCond, newCol);
                        }
                    }
                }
                sparkData = sparkData.where(prevCond);
            }
        }

        return sparkData;
    }

    private Column logicalOperatorHelper(LogicalOperator op, Column col, Column other) {
        if (col == null) {
            return other;
        }
        if (op == LogicalOperator.OR) {
            return col.or(other);
        } else {
            return col.and(other);
        }
    }

    public Dataset<Row> getDbmsTableSparkDF(String query, DatabaseSourceConfig databaseSourceConfig, int limit) throws EnvConfigurationException, IOException {

        String username = databaseSourceConfig.getUsername();
        String password = databaseSourceConfig.getPassword();
        if (isBase64(password)) {
            password = Utils.decodeBase64(password);
        }

        String url = JDBCService.JDBCUrl(databaseSourceConfig);
        query = jdbcService.processQuery(query, databaseSourceConfig.getDbmsType(), limit);

        Dataset<Row> jdbcDF = null;

        if (databaseSourceConfig.getAuthType() != null && databaseSourceConfig.getAuthType().equals(SourceAuthTypeEnum.KEYPAIR)) {
            jdbcDF = sparkSession.read()
                    .format("jdbc")
                    .option("url", url)
                    .option("query", query)
                    .option("user", username)
                    .option("privateKey", databaseSourceConfig.getPrivateKey())
                    .option("privateKeyPassphrase", databaseSourceConfig.getPrivateKeyPassPhrase())
                    .option("driver", jdbcService.getDriver(databaseSourceConfig.getDbmsType()))
                    .load();
        } else {
            jdbcDF = sparkSession.read()
                    .format("jdbc")
                    .option("url", url)
                    .option("query", query)
                    .option("user", username)
                    .option("password", password)
                    .option("driver", jdbcService.getDriver(databaseSourceConfig.getDbmsType()))
                    .load();
        }

        return limit > 0 ? jdbcDF.limit(limit) : jdbcDF;

    }

    @NotNull
    @Transactional
    public Map<String, Object> getFilteredDataset(FilterModel filterModels, UUID datasetId, String branch, UUID transactionId, UUID userId) throws Exception {
        User user = userService.getUserById(userId);

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            throw new NoSuchElementException("No dataset found in catalog for " + datasetId);
        }

        List<ColumnDTO> columns = getDatasetColumns(datasetId, transactionId, branch);

        List<DatasetFiltersDTO> filtersDTOS = new ArrayList<>();
        if (filterModels.getFilterModelList() != null) {
            for (DatasetFilterModel fm : filterModels.getFilterModelList()) {
                filtersDTOS.add(Utils.convertDatasetFilterModelToDatasetFilterDTO(fm));
            }
        }
        Dataset<Row> sparkDataMain = this.getFilteredSparkDataframe(datasetId, branch, String.valueOf(transactionId), "date", filtersDTOS, -1, user.getPreferences().getLanguage());

        if (filterModels.getSort() != null && (filterModels.getSort().getDirection() != null)) {
            if (Objects.equals(filterModels.getSort().getDirection(), "desc")) {
                sparkDataMain = sparkDataMain.orderBy(sparkDataMain.col(filterModels.getSort().getColumn()).desc());
            } else if (Objects.equals(filterModels.getSort().getDirection(), "asc")) {
                sparkDataMain = sparkDataMain.orderBy(sparkDataMain.col(filterModels.getSort().getColumn()).asc());
            }

        }
        Long hits = sparkDataMain.count();
        sparkDataMain = sparkDataMain.limit(500);

        Map<String, Object> response = new HashMap<>();


        List<Map<String, Object>> ab = sparkDataMain.toJSON().collectAsList().stream().map(e -> {
            try {
                return Utils.jacksonStringParser(e, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());

        response.put("table", ab);
        response.put("columns", sparkDataMain.columns().length);
        response.put("rows", ab.size());
        response.put("schema", columns);
        response.put("hits", hits);
        return response;
    }

    @Transactional
    public Dataset<Row> getSparkDF(UUID datasetId, String branch, UUID transactionId, int limit, SparkUDF... udfs) throws Exception {

        for (SparkUDF udf : udfs) {
            sparkSession.udf().register(udf.name, udf.function);
        }

        SchemaModel schemaModel = new SchemaModel();
        CustomSchemaModel customSchema = new CustomSchemaModel();

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingService.getDatasetMapping(datasetId, branch).get().getCurrentTransaction();
        }

        if (schemaRepository.existsByDatasetIdAndBranchAndTransactionId(datasetId, branch, transactionId)) {
            schemaModel = findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId);
            customSchema = schemaModel.getCustomSchema();
        }

        Dataset<Row> dfTotal = querySparkForDF(datasetId, branch, transactionId, schemaModel.getSchema(), customSchema);

        if (limit > -1) {
            dfTotal = dfTotal.limit(limit);
        }

        return dfTotal;
    }

    @Transactional
    public Dataset<Row> querySparkExcel(String[] datasetPaths, ResourceSubtype type, StructType schema, String sheetName) throws Exception {
        // Build dataAddress only when a real sheet name is provided.
        // "first" is a frontend sentinel meaning "use the first sheet".
        // null/empty/first → omit dataAddress so Spark reads the first sheet by default.
        boolean useDefaultSheet = sheetName == null || sheetName.isEmpty() || sheetName.equalsIgnoreCase("first");
        String dataAddress = useDefaultSheet ? null : "'" + sheetName + "'!A1";

        switch (type) {
            case XLS:
                if (schema != null) {
                    DataFrameReader xlsWithSchema = sparkSession.read().format("excel")
                            .schema(schema)
                            .option("header", "true")
                            .option("maxByteArraySize", 1024 * 1024 * 1024);
                    if (dataAddress != null) xlsWithSchema = xlsWithSchema.option("dataAddress", dataAddress);
                    return xlsWithSchema.load(datasetPaths);
                } else {
                    DataFrameReader xlsInfer = sparkSession.read().format("excel")
                            .option("inferSchema", "true")
                            .option("header", "true")
                            .option("maxByteArraySize", 1024 * 1024 * 1024);
                    if (dataAddress != null) xlsInfer = xlsInfer.option("dataAddress", dataAddress);
                    return xlsInfer.load(datasetPaths);
                }

            case XLSX:
                if (schema != null) {
                    DataFrameReader xlsxWithSchema = sparkSession.read().format("excel")
                            .schema(schema)
                            .option("header", "true")
                            .option("maxByteArraySize", 1024 * 1024 * 1024);
                    if (dataAddress != null) xlsxWithSchema = xlsxWithSchema.option("dataAddress", dataAddress);
                    return xlsxWithSchema.load(datasetPaths);
                } else {
                    DataFrameReader xlsxInfer = sparkSession.read().format("excel")
                            .option("inferSchema", "true")
                            .option("header", "true")
                            .option("maxByteArraySize", 1024 * 1024 * 1024);
                    if (dataAddress != null) xlsxInfer = xlsxInfer.option("dataAddress", dataAddress);
                    return xlsxInfer.load(datasetPaths);
                }
        }
        throw new Exception("Not a valid Spark Excel type");
    }

    public void moveFilesToParentDirectory(String sourceDirPath) throws IOException {
        Path sourceDir = Paths.get(sourceDirPath);
        Path parentDir = sourceDir.getParent();

        if (parentDir == null) {
            throw new IllegalArgumentException("The source directory has no parent directory.");
        }

        // Iterate over all files in the source directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    // Move each file to the parent directory
                    Path target = parentDir.resolve(file.getFileName());
                    Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Moved file: " + file.getFileName() + " to " + target);
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to move files from " + sourceDirPath + " to " + parentDir, e);
        }
    }

    public void deleteFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Transactional
    public void convertExcelToParquet(String datasetPath, ResourceSubtype excelType, String fileName, String sheetName) throws Exception {
        Dataset<Row> df = querySparkExcel(new String[]{datasetPath}, excelType, null, sheetName);
        File excelFile = new File(datasetPath + "/" + fileName);
//        Files.move(Paths.get(datasetPath + "/" + "parquet"), Paths.get(datasetPath), StandardCopyOption.REPLACE_EXISTING);
        try {
//            df.write()
//                    .format("csv")
//                    .mode("overwrite")
//                    .option("header", "true")
//                    .save(datasetPath + "/parquet");

//            Dataset<Row> newDf = sparkSession.read()
//                    .format("csv")
//                    .option("sep", ",")
//                    .option("delimiter", "\n")
//                    .option("quote", "\"")
//                    .option("escape", "\"")
//                    .option("multiLine", true)
//                    .option("ignoreLeadingWhiteSpace", true)
//                    .option("inferSchema", "true")
//                    .option("header", "true")
//                    .option("encoding", Charset.defaultCharset().displayName())
//                    .load(datasetPath + "/parquet");
            df.write()
                    .format("parquet")
                    .mode("overwrite")
                    .save(datasetPath + "/parquet");

            Dataset<Row> newDf = sparkSession.read()
                    .format("parquet")
                    .parquet(datasetPath + "/parquet");

            if (excelFile.delete()) {
                log.info("Excel file deleted successfully: " + excelFile.getAbsolutePath());
            } else {
                throw new Exception("Failed to delete excel file");
            }


            moveFilesToParentDirectory(datasetPath + "/parquet");
        } catch (Exception e) {
            throw e;
        }


    }

    @Transactional
    public Dataset<Row> querySparkForDF(UUID datasetId, String branch, UUID transactionId, StructType schema, CustomSchemaModel customSchema) throws Exception {
        BranchModel branchModel = branchService.getBranchModel(datasetId, branch);
        getResourcePath("dataset", datasetId, String.valueOf(transactionId));
        List<String> transactions = datasetMappingService.getTransactions(datasetId, branch, transactionId);
        String[] datasetPaths = transactions.stream().map(tId -> getResourcePath("dataset", datasetId, String.valueOf(tId))).toArray(String[]::new);
        Dataset<Row> dfTotal = null;
        switch (branchModel.getType()) {
            case JSON:
                if (schema != null) {
                    dfTotal = sparkSession.read()
                            .schema(schema)
                            .option("mode", "PERMISSIVE")
                            .option("multiline", "true")
                            .option("allowComments", "true")
                            .option("ignoreNullFields", "false")
                            .json(datasetPaths);
                } else {
                    dfTotal = sparkSession.read()
                            .option("mode", "PERMISSIVE")
                            .option("multiline", "true")
                            .option("allowComments", "true")
                            .option("ignoreNullFields", "false")
                            .json(datasetPaths);
                }
                break;
            case XLS:
            case XLSX:
                dfTotal = querySparkExcel(datasetPaths, branchModel.getType(), schema, null);
                break;
            case CSV:
            case RAWDATASET: {
                if (schema != null) {
                    dfTotal = sparkSession.read()
                            .schema(schema)
                            .format("csv")
                            .option("sep", customSchema.getFieldDelimiter())
                            .option("delimiter", customSchema.getFieldDelimiter())
                            .option("quote", customSchema.getEscapeCharacter())
                            .option("escape", customSchema.getEscapeCharacter())
                            .option("multiLine", true)
                            .option("ignoreLeadingWhiteSpace", true)
                            .option("header", customSchema.isHeader())
                            .option("encoding", customSchema.getEncoding())
                            .load(datasetPaths);
                } else {
                    dfTotal = sparkSession.read()
                            .format("csv")
                            .option("sep", customSchema.getFieldDelimiter())
                            .option("delimiter", customSchema.getFieldDelimiter())
                            .option("quote", customSchema.getEscapeCharacter())
                            .option("escape", customSchema.getEscapeCharacter())
                            .option("multiLine", true)
                            .option("ignoreLeadingWhiteSpace", true)
                            .option("inferSchema", "true")
                            .option("header", customSchema.isHeader())
                            .option("encoding", customSchema.getEncoding())
                            .load(datasetPaths);
                }

                break;
            }
            case PARQUET:
            case BUILDDATASET: {
                dfTotal = sparkSession.read()
                        .schema(schema)
                        .format("parquet")
                        .option("header", customSchema == null || customSchema.isHeader())
                        .parquet(datasetPaths);
                break;
            }
            case LIVEDATASET: {
                Link link = linkRepository.findByDatasetIdAndBranch(datasetId, branch).orElseThrow();
                Source source = sourceService.findById(link.getSourceId());
                DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(source.getSourceConfig());
                dfTotal = getDbmsTableSparkDF(link.getScript(), databaseSourceConfig, -1);
                break;
            }
            default: {
                throw new RuntimeException("Not a valid type of dataset file type present in branch model " + branchModel.getType());
            }
        }

        return dfTotal;
    }

    @Transactional
    public ManualProcessingResultDTO applyManualProcessing(ResourceSubtype fileType, Dataset<Row> dfTotal) {
        HashMap<String, String> dateFormatMap = new HashMap<>();

//        if (!(fileType.equals(ResourceSubtype.PARQUET) || fileType.equals(ResourceSubtype.BUILDDATASET))) {
//            try {
//                for (StructField field : dfTotal.schema().fields()) {
//                    if (field.dataType().typeName().equalsIgnoreCase("timestamp")) {
//                        dateFormatMap.put(field.name(), "yyyy-MM-dd HH:mm:ss. SSSS");
//                    } else if (field.dataType().typeName().equalsIgnoreCase("date")) {
//                        dateFormatMap.put(field.name(), "yyyy-MM-dd");
//                    } else if (field.dataType().typeName().equalsIgnoreCase("string")) {
//                        String dateString = String.valueOf(dfTotal.limit(1).select(col("`" + field.name() + "`")).collectAsList().get(0).get(0));
//
//                        if (dateString.matches(".*[0987654321].*")) {
//                            DateFormatDTO dateFormatDTO = Utils.detectDateFormat(dateString);
//                            if (dateFormatDTO != null) {
//                                String format = dateFormatDTO.getFormat();
//                                dateFormatMap.put(field.name(), format);
//                                dfTotal = dfTotal.withColumn(field.name(), call_udf("stringToTimestamp", col(field.name()), lit(format)));
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error in detecting date : " + e.getMessage());
////            e.printStackTrace();
//            }
//            Dataset<Row> newDf = dfTotal;
//            for (StructField element : dfTotal.schema().fields()) {
//                if (element.dataType().typeName().equalsIgnoreCase("timestamp")) {
//                    String format = null;
//                    if (dateFormatMap.containsKey(element.name())) {
//                        format = dateFormatMap.get(element.name());
//                    } else {
//                        String dateString = String.valueOf(dfTotal.limit(1).select(col(element.name())).collectAsList().get(0).get(0));
//                        DateFormatDTO dateFormat = Utils.detectDateFormat(dateString);
//                        if (dateFormat != null) {
//                            format = dateFormat.getFormat();
//                        }
//                    }
//
//                    StructField currentFieldFromDataFrame = null;
//                    for (StructField field : dfTotal.schema().fields()) {
//                        if (field.name().equalsIgnoreCase(element.name())) {
//                            currentFieldFromDataFrame = field;
//                        }
//                    }
//
//                    if (currentFieldFromDataFrame != null && format != null && currentFieldFromDataFrame.dataType().typeName().equalsIgnoreCase("string")) {
//                        newDf = newDf.withColumn(element.name(), call_udf("stringToTimestamp", col(element.name()), lit(format)));
//
//                        dateFormatMap.put(element.name(), format);
//                    }
//                }
//            }
//        for (StructField field : dfTotal.schema().fields()) {
//            convertColumnWrapper(datasetId, branch, transactionId, "timestamp", "Transaction_date", "")
//        }


        ManualProcessingResultDTO result = new ManualProcessingResultDTO();
        result.setDateFormatMap(dateFormatMap);
        result.setDfTotal(dfTotal);
        return result;
    }

    @Transactional
    public void createDBSchemaIfNotExists(UUID datasetId, String branch, UUID transactionId, ResourceSubtype fileType, UUID userId) throws Exception {
        SchemaModel schemaModel = new SchemaModel();
        CustomSchemaModel customSchemaModel = new CustomSchemaModel();
        Dataset<Row> dfTotal = getSparkDF(datasetId, branch, transactionId, -1);
        ManualProcessingResultDTO manualProcessingResult = applyManualProcessing(fileType, dfTotal);
        dfTotal = manualProcessingResult.getDfTotal();

        HashMap<String, String> dateFormatMap = manualProcessingResult.getDateFormatMap();
        customSchemaModel.setDateFormat(dateFormatMap);

        schemaModel.setDatasetId(datasetId);
        schemaModel.setBranch(branch);
        schemaModel.setTransactionId(transactionId);
        schemaModel.setSchema(dfTotal.schema());
        schemaModel.setCustomSchema(customSchemaModel);
        schemaModel.setStatus("active");
        schemaModel.setCreatedBy(userId);

        schemaRepository.save(schemaModel);
    }

    @Transactional
    public CustomSchemaApplyResultDTO handleCustomSchemaApply(UUID datasetId, String branch, UUID transactionId, CustomSchemaModel customSchemaModel) throws Exception {
        CustomSchemaApplyResultDTO result = new CustomSchemaApplyResultDTO();
        BranchModel branchModel = branchService.getBranchModel(datasetId, branch);

        Dataset<Row> df = querySparkForDF(datasetId, branch, transactionId, null, customSchemaModel);
        ManualProcessingResultDTO manualProcessingResult = applyManualProcessing(branchModel.getType(), df);
        result.setSchema(manualProcessingResult.getDfTotal().schema());
        result.setDateFormatMap(manualProcessingResult.getDateFormatMap());
        return result;
    }

    @Transactional
    public List<ColumnDTO> getDatasetColumns(UUID datasetId, UUID transactionId, String branch) throws Exception {
        List<ColumnDTO> colList = new ArrayList<>();
        Dataset<Row> df = getSparkDF(datasetId, branch, transactionId, 1);
        Arrays.stream(df.schema().fields()).forEach((element) -> colList.add(ColumnDTO.builder().headerName(element.name()).field(element.name()).type(element.dataType().typeName()).build()));

        return colList;
    }

    @Transactional
    public SchemaModel findByDatasetIdAndBranchAndTransactionIdAndStatus(UUID datasetId, String branch, UUID transactionId) throws Exception {
        try {
            return schemaRepository.findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId, "active");
        } catch (Exception e) {
            BranchModel branchModel = branchService.getBranchModel(datasetId, branch);
            schemaRepository.deleteAllByDatasetIdAndBranchAndTransactionId(datasetId, branch, transactionId);
            createDBSchemaIfNotExists(datasetId, branch, transactionId, branchModel.getType(), branchModel.getUpdatedBy());
            return schemaRepository.findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId, "active");
        }
    }

    @Transactional
    public Map<String, Object> getDatasetSchemaForFunnel(UUID datasetId, UUID transactionId, String branch) throws Exception {
        Map<String, Object> result = new HashMap<>();

        SchemaModel schemaModel = null;
        CustomSchemaModel customSchema = new CustomSchemaModel();

        if (!schemaRepository.existsByDatasetIdAndBranchAndTransactionId(datasetId, branch, transactionId)) {
            // Create Schema
            BranchModel branchModel = branchService.getBranchModel(datasetId, branch);
            createDBSchemaIfNotExists(datasetId, branch, transactionId, branchModel.getType(), null);
        }
        schemaModel = findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId);
        customSchema = schemaModel.getCustomSchema();

        Dataset<Row> df = getSparkDF(datasetId, branch, transactionId, 10);
        result.put("schema", df.schema());
        result.put("customSchema", customSchema);

        return result;
    }

    @Transactional
    public void updateDatasetCustomSchemaForFunnel(UUID datasetId, UUID transactionId, String branch, CsvPreprocessingDTO csvPreprocessingDTO) throws Exception {
        SchemaModel schemaModel = null;
        CustomSchemaModel customSchema = new CustomSchemaModel();
        csvPreprocessingService.getCustomSchemaFromCsvPreprocessing(customSchema, csvPreprocessingDTO);

        if (!schemaRepository.existsByDatasetIdAndBranchAndTransactionId(datasetId, branch, transactionId)) {
            BranchModel branchModel = branchService.getBranchModel(datasetId, branch);
            createDBSchemaIfNotExists(datasetId, branch, transactionId, branchModel.getType(), null);
        }
        schemaModel = findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId);
        CustomSchemaApplyResultDTO customSchemaApplyResult = handleCustomSchemaApply(datasetId, branch, transactionId, customSchema);

        StructType schema = customSchemaApplyResult.getSchema();
        schemaModel.setCustomSchema(customSchema);
        schemaModel.setSchema(schema);

        schemaRepository.save(schemaModel);
    }

    @Transactional
    public List<Map<String, Object>> getSchemasOfSources(List<SourceDataset> sources, List<UUID> sourcesTransactionIds) throws Exception {
        List<Map<String, Object>> schemas = new ArrayList<>();
        for (int idx = 0; idx < sources.size(); idx++) {
            UUID datasetId = UUID.fromString(sources.get(idx).getSource());
            String branch = sources.get(idx).getBranch();
            BranchModel branchModel = branchService.getBranchModel(datasetId, branch);
            switch (branchModel.getType()) {
                case JSON:
                case CSV:
                case XLS:
                case RAWDATASET: {
                    schemas.add(getDatasetSchemaForFunnel(datasetId, sourcesTransactionIds.get(idx), branch));
                    break;
                }
                case PARQUET:
                case LIVEDATASET:
                case BUILDDATASET: {
                    schemas.add(new HashMap<>());
                    break;
                }
                default:
                    throw new Exception("Unsupported dataset type : " + branchModel.getType());
            }
        }

        return schemas;
    }

    public Dataset<Row> processFilesToSparkDf(String path) throws Exception {
        return sparkSession.read()
                .option("header", "true").csv(path);
    }

    @Transactional
    public Dataset<Row> convertStringToDate(Dataset<Row> dfTotal, String columnName) throws Exception {
        Dataset<Row> newDf = dfTotal.withColumn(columnName, dfTotal.col(columnName).cast("string"));
        return newDf.withColumn(columnName, coalesce(
                functions.to_date(newDf.col(columnName), "yyyy-MM-dd"),
                functions.to_date(newDf.col(columnName), "dd-MM-yyyy"),
                functions.to_date(newDf.col(columnName), "MM-dd-yyyy"),
                functions.to_date(newDf.col(columnName), "dd/MM/yyyy"),
                functions.to_date(newDf.col(columnName), "MM/dd/yyyy"),
                functions.to_date(newDf.col(columnName), "yyyy/MM/dd"),
                functions.to_date(functions.from_unixtime(newDf.col(columnName))),
                functions.to_date(functions.from_unixtime(newDf.col(columnName).divide(1000)))
        ));
    }

    @Transactional
    public Dataset<Row> convertStringToTimeStamp(Dataset<Row> dfTotal, String columnName) throws Exception {
        // Casting to string because, for unix time casting hack
        // Casting to unix time only works on string, but usually when spark reads for the first time it read unix time as non string
        // Hence converting everything to string first
        Dataset<Row> newDf = dfTotal.withColumn(columnName, dfTotal.col(columnName).cast("string"));

        newDf = newDf.withColumn(columnName,
                coalesce(
                        functions.to_timestamp(newDf.col(columnName), "yyyy-MM-dd HH:mm:ss"),
                        functions.to_timestamp(newDf.col(columnName), "dd-MM-yyyy HH:mm:ss"),
                        functions.to_timestamp(newDf.col(columnName), "MM-dd-yyyy HH:mm:ss"),
                        functions.to_timestamp(newDf.col(columnName), "dd/MM/yyyy HH:mm:ss"),
                        functions.to_timestamp(newDf.col(columnName), "MM/dd/yyyy HH:mm:ss"),
                        functions.to_timestamp(newDf.col(columnName), "yyyy/MM/dd HH:mm:ss"),

                        functions.to_timestamp(newDf.col(columnName), "yyyy-MM-dd HH:mm"),
                        functions.to_timestamp(newDf.col(columnName), "dd-MM-yyyy HH:mm"),
                        functions.to_timestamp(newDf.col(columnName), "MM-dd-yyyy HH:mm"),
                        functions.to_timestamp(newDf.col(columnName), "dd/MM/yyyy HH:mm"),
                        functions.to_timestamp(newDf.col(columnName), "MM/dd/yyyy HH:mm"),
                        functions.to_timestamp(newDf.col(columnName), "yyyy/MM/dd HH:mm"),

                        functions.to_timestamp(newDf.col(columnName), "yyyy-MM-dd HH"),
                        functions.to_timestamp(newDf.col(columnName), "dd-MM-yyyy HH"),
                        functions.to_timestamp(newDf.col(columnName), "MM-dd-yyyy HH"),
                        functions.to_timestamp(newDf.col(columnName), "dd/MM/yyyy HH"),
                        functions.to_timestamp(newDf.col(columnName), "MM/dd/yyyy HH"),
                        functions.to_timestamp(newDf.col(columnName), "yyyy/MM/dd HH"),

                        functions.to_timestamp(newDf.col(columnName), "yyyy-MM-dd"),
                        functions.to_timestamp(newDf.col(columnName), "dd-MM-yyyy"),
                        functions.to_timestamp(newDf.col(columnName), "MM-dd-yyyy"),
                        functions.to_timestamp(newDf.col(columnName), "dd/MM/yyyy"),
                        functions.to_timestamp(newDf.col(columnName), "MM/dd/yyyy"),
                        functions.to_timestamp(newDf.col(columnName), "yyyy/MM/dd"),
                        functions.to_timestamp(functions.from_unixtime(newDf.col(columnName))),
                        functions.to_timestamp(functions.from_unixtime(newDf.col(columnName).divide(1000)))
                ));

        return newDf;
    }

    @Transactional
    public void convertColumnWrapper(UUID datasetId, String branch, UUID transactionId, String conversionType, String columnName, String format, StructType schema, CustomSchemaModel customSchema) throws Exception {
        Dataset<Row> dfTotal = querySparkForDF(datasetId, branch, transactionId, schema, customSchema);
        String datasetPath = getResourcePath("dataset", datasetId, String.valueOf(transactionId));
        String datasetPathTmp = datasetPath + "/tmp";
        Dataset<Row> convertedDf = null;
        if (conversionType.equals("timestamp")) {
            convertedDf = convertStringToTimeStamp(dfTotal, columnName);
        } else if (conversionType.equals("date")) {
            convertedDf = convertStringToDate(dfTotal, columnName);
        } else {
            throw new Exception("Unsupported conversion type: " + conversionType);
        }

        BranchModel branchModel = branchService.getBranchModel(datasetId, branch);
        switch (branchModel.getType()) {
            case JSON:
                convertedDf.write().mode("overwrite").json(datasetPathTmp);
            case XLS:
            case XLSX:
                convertedDf.write().mode("overwrite").parquet(datasetPathTmp);
                break;
            case CSV:
            case RAWDATASET:
                convertedDf.write().mode("overwrite").csv(datasetPathTmp);
                break;
            case BUILDDATASET:
            case LIVEDATASET:
                convertedDf.write().mode("overwrite").parquet(datasetPathTmp);
                break;
            default:
                throw new Exception(branchModel.getType() + " not a valid dataset type in branch model");
        }

        deleteFilesInDirectory(datasetPath);
        moveFilesToParentDirectory(datasetPathTmp);
    }

    @Transactional
    public void handleChangedColumnsTypes(StructType oldSchema, StructType newSchema, UUID datasetId, String branch, UUID transactionId, StructType schema, CustomSchemaModel customSchema) throws Exception {
        Map<String, DataType> oldSchemaMap = new HashMap<>();
        for (StructField field : oldSchema.fields()) {
            oldSchemaMap.put(field.name(), field.dataType());
        }

        Map<String, DataType> newSchemaMap = new HashMap<>();
        for (StructField field : newSchema.fields()) {
            newSchemaMap.put(field.name(), field.dataType());
        }

        for (String columnName : newSchemaMap.keySet()) {
            DataType newDataType = newSchemaMap.get(columnName);
            DataType oldDataType = oldSchemaMap.get(columnName);

            if (oldDataType != null) {
                if (!newDataType.equals(oldDataType)) {
                    if (newDataType instanceof TimestampType) {
                        convertColumnWrapper(datasetId, branch, transactionId, "timestamp", columnName, "", schema, customSchema);
                    } else if (newDataType instanceof DateType) {
                        convertColumnWrapper(datasetId, branch, transactionId, "date", columnName, "", schema, customSchema);
                    }
                }
            }
        }
    }

    @Transactional
    public Dataset<Row> jsonToDataFrame(String jsonString) throws IOException {
        String decodedJsonString = decodeJson(jsonString);
        Dataset<String> jsonDataset = sparkSession.createDataset(
                Collections.singletonList(decodedJsonString),
                Encoders.STRING()
        );

        return sparkSession.read()
                .option("mode", "PERMISSIVE")
                .option("multiline", "true")
                .option("allowComments", "true")
                .option("ignoreNullFields", "false")
                .json(jsonDataset);
    }

    @Transactional
    public Dataset<Row> csvToDataFrame(String csv, CsvPreprocessingDTO csvPreprocessing) throws IOException {
        List<String> csvLines = Arrays.asList(csv.split("\n"));
        Dataset<String> csvData = sparkSession.createDataset(csvLines, Encoders.STRING());
        // Use Spark's CSV reader on the RDD
        return csvPreprocessingService.getProcessedCsv(csvData, csvPreprocessing);
    }

    @Transactional
    public void writeDataFrameAsJson(Dataset<Row> df, UUID datasetId, UUID transactionId) {
        String datasetPath = getResourcePath("dataset", datasetId, String.valueOf(transactionId));
        df.write()
                .mode("overwrite")
                .json(datasetPath);
    }

    @Transactional
    public void writeDataFrameAsCsv(Dataset<Row> df, UUID datasetId, UUID transactionId) {
        String datasetPath = getResourcePath("dataset", datasetId, String.valueOf(transactionId));
        df.coalesce(1).write()
                .mode("overwrite")
                .format("csv")
                .csv(datasetPath);
    }

    public String decodeJson(String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonData);
        decodeJsonNode(rootNode);
        return mapper.writeValueAsString(rootNode); // Return the modified JSON as a string
    }

    private void decodeJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<Map.Entry<String, JsonNode>> entriesToRename = new ArrayList<>();

            // Collect entries that need renaming to avoid concurrent modification
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String originalKey = entry.getKey();
                String decodedKey = URLDecoder.decode(originalKey, StandardCharsets.UTF_8)
                        .replaceAll("^\"|\"$", ""); // Remove surrounding quotes

                if (!decodedKey.equals(originalKey)) {
                    entriesToRename.add(Map.entry(originalKey, entry.getValue())); // Add entry to list if it needs renaming
                }

                // Recursively decode nested objects or arrays
                decodeJsonNode(entry.getValue());
            }

            // Apply renaming after the iteration
            for (Map.Entry<String, JsonNode> entry : entriesToRename) {
                String originalKey = entry.getKey();
                String decodedKey = URLDecoder.decode(originalKey, StandardCharsets.UTF_8)
                        .replaceAll("^\"|\"$", ""); // Remove surrounding quotes
                JsonNode value = entry.getValue();
                objectNode.remove(originalKey); // Remove the original entry
                objectNode.set(decodedKey, value); // Set the entry with the decoded key
            }

        } else if (node.isArray()) {
            // Iterate over each element in the JSON array
            for (JsonNode arrayItem : node) {
                decodeJsonNode(arrayItem); // Recursively decode array elements
            }
        }
    }

}
