package io.orphea.zoro.library.services;


import io.orphea.bob.library.models.SocketMessage;
import io.orphea.bob.library.services.BuildService;
import io.orphea.kitab.library.models.BranchModel;
import io.orphea.kitab.library.models.DatasetStatsModel;
import io.orphea.kitab.library.repository.BranchRepository;
import io.orphea.kitab.library.repository.DatasetStatsRepository;
import io.orphea.sharedUtils.BackingFS;
import io.orphea.zoro.library.models.CustomSchemaModel;
import io.orphea.zoro.library.models.SchemaModel;
import io.orphea.zoro.library.repository.SchemaRepository;
import io.kubernetes.client.openapi.ApiException;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.orphea.sharedUtils.Utils.isBase64;
import static org.apache.spark.sql.functions.*;

;

@Component
@RequiredArgsConstructor
public class ZoroService {

    private final DatasetStatsRepository datasetStatsRepository;
    private final BranchRepository branchRepository;
    private final SchemaRepository schemaRepository;
    private final BuildService buildService;

    @Autowired
    SimpMessagingTemplate template;

    public static String underscoreToCamelCase(String underscoreName) {
        StringBuilder result = new StringBuilder();
        if (underscoreName != null && underscoreName.length() > 0) {
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ("_".charAt(0) == ch) {
                    flag = true;
                } else {
                    if (flag) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }

    public SparkSession sparkSession() throws Exception {
        String applicationName = UUID.randomUUID().toString();
        SparkSession spark = null;
        String backingFS = System.getenv("BACKING_FS");
        switch (backingFS) {
            case "s3":
                spark = createS3Session(applicationName);
                break;
            case "gs":
                spark = createGSSession(applicationName);
                break;
            case "hdfs":
                spark = createHDFSSession(applicationName);
                break;
            default:
                throw new Exception("Error: no backing FS defined");
        }
        return spark;
    }

    private SparkSession createS3Session(String applicationName) throws IOException, ApiException {
        return SparkSession.builder()
                .appName(applicationName)
                .master(sparkMaster())
                .config("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"))
                .config("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"))
                .config("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"))
                .config("spark.hadoop.fs.s3a.path.style.access", true)
                .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .getOrCreate();
    }

    private SparkSession createGSSession(String applicationName) throws IOException, ApiException {
        String googleCloudCredentials = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        if (isBase64(googleCloudCredentials)) {
            googleCloudCredentials = new String(Base64.decodeBase64(googleCloudCredentials));
        }
        File file = new File(System.getenv("ORPHEA_MOUNT_PATH") + "/google_creds.json");
        try (FileWriter fileWriter = new FileWriter(file.getPath())) {
            fileWriter.write(googleCloudCredentials);
        }
        return SparkSession.builder()
                .appName(applicationName)
                .master(sparkMaster())
                .config("google.cloud.auth.service.account.enable", "true")
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .config("google.cloud.auth.service.account.json.keyfile", System.getenv("ORPHEA_MOUNT_PATH") + "/google_creds.json")
                .getOrCreate();
    }

    private SparkSession createHDFSSession(String applicationName) throws IOException, ApiException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
        return SparkSession.builder()
                .appName(applicationName)
                .master(sparkMaster())
                .config("spark.deploy.mode", "cluster")

                .config("spark.kubernetes.namespace", "orphea")

//                    .config("spark.driver.host", "boson-spark-driver-pod.boson.orphea")
//                    .config("spark.kubernetes.driver.pod.name", "boson-spark-driver-pod")

                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .config("spark.kubernetes.authenticate.driver.serviceAccountName", "spark")
                .config("spark.kubernetes.container.image", System.getenv("SPARK_PYSPARK_IMAGE"))
                .config("spark.kubernetes.container.image.pullPolicy", "Always")

                .config("spark.hadoop.fs.defaultFS", System.getenv("HDFS_ENDPOINT"))

                .getOrCreate();

    }

    public SparkSession sparkSession1() throws Exception {
        String applicationName = UUID.randomUUID().toString();


//        SparkConf conf = new SparkConf().setMaster("k8s://https://" + controlPlaneIP).set("spark.driver.host", "localhost").set("spark.deploy.mode", "client");
//        SparkSession spark = SparkSession.builder().config(conf).getOrCreate();
//        SparkContext sc = spark.sparkContext();
        // Perform Spark actions here
//        spark.stop();


//         */

        SparkSession spark = null;

        if (Objects.equals(System.getenv("BACKING_FS"), "s3")) {

            // TODO : run this in kuebernets spark not on locaL[*]
            spark = SparkSession.builder()

                    .appName(applicationName)
                    .master(sparkMaster())
//                    .config("spark.driver.host", "localhost")
//                    .config("spark.deploy.mode", "client")
//                    .config("spark.ui.showConsoleProgress", true)
//                    .config("spark.executor.cores", 1)
//                    .config("spark.worker.cores", 1)
//                    .config("spark.ui.enabled", false)

                    .config("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"))
                    .config("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"))
                    .config("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"))
                    .config("spark.hadoop.fs.s3a.path.style.access", true)
                    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")

                    .getOrCreate();

            spark.sparkContext().setLogLevel("WARN");

        } else if (Objects.equals(System.getenv("BACKING_FS"), "gs")) {

            String google_cloud_credentials;

            if (isBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS"))) {
                google_cloud_credentials = new String(Base64.decodeBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS")));
            } else {
                google_cloud_credentials = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
            }

            // TODO : don't write into the filesystem... use it from memory
            File file = new File(System.getenv("ORPHEA_MOUNT_PATH") + "/google_creds.json");
            FileWriter fileWriter = new FileWriter(file.getPath());
            fileWriter.write(google_cloud_credentials);
            fileWriter.close();

            spark = SparkSession.builder()

                    .appName(applicationName)
                    .master(sparkMaster())
//                    .config("spark.driver.host", "localhost")
//                    .config("spark.deploy.mode", "client")
//                    .config("spark.ui.showConsoleProgress", false)
//                    .config("spark.executor.cores", 1)
//                    .config("spark.worker.cores", 1)
//                    .config("spark.ui.enabled", false)

////                    .config("spark.master", "k8s://https://10.0.0.1:443")
//                    .config("spark.deploy.mode", "cluster")
//                    .config("spark.kubernetes.namespace", "orphea")
//                    .config("spark.app.name", "orphea-spark-942199fc7efa")
//                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
//                    .config("spark.kubernetes.authenticate.driver.serviceAccountName", "spark")
//                    .config("spark.kubernetes.container.image", System.getenv("SPARK_PYSPARK_IMAGE"))

                    .config("google.cloud.auth.service.account.enable", "true")
                    .config("google.cloud.auth.service.account.json.keyfile", System.getenv("ORPHEA_MOUNT_PATH") + "/google_creds.json")

                    .getOrCreate();

            spark.sparkContext().setLogLevel("WARN");
            System.out.println("debug spark HDFS");
            System.out.println();
            System.out.println(Arrays.toString(spark.sparkContext().getConf().getAll()));
            System.out.println(spark.version());

            System.out.println("debug spark HDFS");

        } else if (Objects.equals(System.getenv("BACKING_FS"), "hdfs")) {

            // Set the Hadoop FS URI and HDFS configuration properties
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));

            spark = SparkSession.builder()

                    .appName(applicationName)
                    .master(sparkMaster())
//                    .config("spark.driver.host", "localhost")
//                    .config("spark.deploy.mode", "client")
//                    .config("spark.ui.showConsoleProgress", true)
//                    .config("spark.executor.cores", 1)
//                    .config("spark.worker.cores", 1)
//                    .config("spark.ui.enabled", false)

                    .config("spark.hadoop.fs.defaultFS", System.getenv("HDFS_ENDPOINT"))

                    .getOrCreate();

            spark.sparkContext().setLogLevel("WARN");

            System.out.println("debug spark HDFS");
            System.out.println();
            System.out.println(Arrays.toString(spark.sparkContext().getConf().getAll()));
            System.out.println(spark.version());

            System.out.println("debug spark HDFS");

        } else {
            throw new Exception("Error : no backing FS defined");
        }

        return spark;
    }

    public Dataset<Row> getSparkDF(UUID datasetId, String branch, int limit) throws Exception {
        SparkSession spark = sparkSession();
        Dataset<Row> dfTotal = null;
        try {

            BranchModel branchModel = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);

            String datasetPath = BackingFS.getDatasetPath(datasetId, branch);

            if (branchModel.getType().equals("raw")) {
                if (!schemaRepository.existsByDatasetIdAndBranch(datasetId, branch)) { // if no schema found then create it in db
                    createDBSchemaIfNotExists(datasetId, branch);
                }

                SchemaModel schemaModelActive = schemaRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");

                Dataset<Row> reader = spark.read()
                        .format("csv")
                        .option("sep", schemaModelActive.getCustomSchema().getFieldDelimiter())
                        .option("delimiter", schemaModelActive.getCustomSchema().getFieldDelimiter())
                        .option("quote", schemaModelActive.getCustomSchema().getEscapeCharacter())
                        .option("escape", schemaModelActive.getCustomSchema().getEscapeCharacter())
//                        .option("timestampFormat", schemaModelActive.getCustomSchema().getDateFormat().get(0).get("timestampDefault"))
//                        .option("dateFormat", schemaModelActive.getCustomSchema().getDateFormat().get(0).get("dateDefault"))
                        .option("ignoreLeadingWhiteSpace", true)
                        .schema(schemaModelActive.getSchema())
                        .option("header", "true")
                        .load(datasetPath);

                if (limit > -1) {
                    dfTotal = reader.limit(limit);
                } else {
                    dfTotal = reader;
                }


//                dfTotal.show();

                //            spark.sql("set spark.sql.legacy.timeParserPolicy=LEGACY");

                String preDefinedFormat = "";
//                Map<String, String> dateFormatMap = schemaModelActive.getCustomSchema().getDateFormat().get(0);
//
//                for (StructField element : schemaModelActive.getSchema().fields()) {
//                    if (dateFormatMap.containsKey(element.name())) {
//                        preDefinedFormat = dateFormatMap.get(element.name());
//                    }
//
//                    if (element.dataType().typeName().equals("timestamp")) {
//                        preDefinedFormat = dateFormatMap.getOrDefault("timestampDefault", preDefinedFormat);
//                        dfTotal = dfTotal.withColumn(element.name(), to_timestamp(col(element.name()), preDefinedFormat));
//                    } else if (element.dataType().typeName().equalsIgnoreCase("date")) {
//                        preDefinedFormat = dateFormatMap.getOrDefault("dateDefault", preDefinedFormat);
//                        dfTotal = dfTotal.withColumn(element.name(), to_date(col(element.name()), preDefinedFormat));
//                    }
//                }

            } else {
                Dataset<Row> reader = spark.read()
                        .format("parquet")
                        .option("header", "true")
                        .parquet(datasetPath);

                if (limit > -1) {
                    dfTotal = reader.limit(limit);
                } else {
                    dfTotal = reader;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dfTotal;
    }

    public void createDBSchemaIfNotExists(UUID datasetId, String branch) throws Exception {

        try {
            SparkSession spark = sparkSession();
            BranchModel branchModel = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);
            Dataset<Row> dfTotal = null;

            CustomSchemaModel customSchema = new CustomSchemaModel();

            HashMap<String, String> dateFormat = new HashMap<>();
            dateFormat.put("dateDefault", "MM/dd/yyyy");
            dateFormat.put("timestampDefault", "MM/dd/yyyy HH:mm");
            List<HashMap<String, String>> dateFormats = new ArrayList<>();
            dateFormats.add(dateFormat);
//            customSchema.setDateFormat(dateFormats);

            String datasetPath = BackingFS.getDatasetPath(datasetId, branch);
            if (branchModel.getType().equals("raw")) {
                dfTotal = spark.read()
                        .format("csv")
                        .option("inferSchema", "true")
                        .option("header", "true")
                        .option("nullValue", "null")
                        .load(datasetPath)
                        .limit(500);
            } else if (branchModel.getType().equals("parquet")) {
                dfTotal = spark.read()
                        .format("parquet")
                        .option("header", "true")
                        .parquet(datasetPath)
                        .limit(500);
            } else {
                throw new RuntimeException("Branch Type is not valid.");
            }

            assert dfTotal != null;

            customSchema.setLineDelimiter(detectLineDelimiter(dfTotal));
            customSchema.setFieldDelimiter(detectFieldDelimiter(dfTotal));
            customSchema.setEscapeCharacter("\"");

            // Re-read with correct delimiters etc
            if (branchModel.getType().equals("raw")) {
                dfTotal = spark.read()
                        .format("csv")
                        .option("sep", customSchema.getFieldDelimiter())
                        .option("delimiter", customSchema.getFieldDelimiter())
                        .option("quote", customSchema.getEscapeCharacter())
                        .option("escape", customSchema.getEscapeCharacter())
                        .option("ignoreLeadingWhiteSpace", true)
                        .option("inferSchema", "true")
                        .option("header", "true")
                        .load(datasetPath)
                        .limit(0);
            }

            Dataset<Row> df = dfTotal;

            SchemaModel schemaModel;
            if (schemaRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                schemaModel = schemaRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");
                schemaModel.setSchema(df.schema());
                schemaModel.setCustomSchema(customSchema);
                schemaModel.setUpdatedBy(UUID.fromString("500fb9d6-42af-4512-88c9-a12e7fb7053e")); // TODO: parameterize
                schemaModel.setUpdatedAt(new Date());
            } else {
                schemaModel = new SchemaModel();
                schemaModel.setDatasetId(datasetId);
                schemaModel.setBranch(branch);
                schemaModel.setSchema(df.schema());
                schemaModel.setCustomSchema(customSchema);
                schemaModel.setStatus("active");
                schemaModel.setCreatedBy(UUID.fromString("500fb9d6-42af-4512-88c9-a12e7fb7053e")); // TODO: parameterize
            }
            schemaRepository.saveAndFlush(schemaModel);

        } catch (RuntimeException e) {
            System.err.println("Failed to create DB schema for datasetId: " + datasetId + " and branch: " + branch);
            throw e;
        }
    }

    public StructType getSchema(UUID datasetId, String branch, CustomSchemaModel customSchemaModel) throws Exception {
        SparkSession spark = sparkSession();
        BranchModel branchModel = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);

        String datasetPath = BackingFS.getDatasetPath(datasetId, branch);

        Dataset<Row> dfTotal;
        if (branchModel.getType().equals("raw")) {
            dfTotal = spark.read()
                    .format("csv")
                    .option("sep", customSchemaModel.getFieldDelimiter())
                    .option("delimiter", customSchemaModel.getFieldDelimiter())
                    .option("quote", customSchemaModel.getEscapeCharacter())
                    .option("escape", customSchemaModel.getEscapeCharacter())
                    .option("ignoreLeadingWhiteSpace", true)
                    .option("inferSchema", "true")
                    .option("header", "true")
                    .csv(datasetPath)
                    .limit(1);
        } else {
            dfTotal = spark.read()
                    .format("parquet")
                    .option("header", "true")
                    .parquet(datasetPath)
                    .limit(1);
        }
        return dfTotal.schema();
    }

    @Async
    public void statsCalculation(UUID userId, UUID datasetId, String branch) throws Exception {
        Dataset<Row> dfTotal = getSparkDF(datasetId, branch, -1);

        long totalRows = dfTotal.count(); // gives number of rows
        long totalColumns = dfTotal.columns().length; // gives number of rows

        List<Map<String, Object>> listOfFiles = BackingFS.getListOfFiles(datasetId, branch);

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

    private String detectFieldDelimiter(Dataset<Row> dfTotal) throws Exception {

        return ",";

        // TODO : below needs re-doing because it is slowing the uploads

        /*
        Map<String, Long> delimiterCounts = new HashMap<>();
        delimiterCounts.put(",", 0L);
        delimiterCounts.put("\t", 0L);
        delimiterCounts.put(";", 0L);
        delimiterCounts.put("|", 0L);
        delimiterCounts.put("#", 0L);

//        for (String columnName : dfTotal.columns()) {
//            for (String delimiter : delimiterCounts.keySet()) {
//                long count = dfTotal.filter(dfTotal.col(columnName).like("%" + delimiter + "%")).count();
//                delimiterCounts.put(delimiter, delimiterCounts.get(delimiter) + count);
//            }
//        }

        for (String delimiter : delimiterCounts.keySet()) {
            long count = dfTotal.javaRDD().filter(word -> word.equals(delimiter)).count();
//            long count = dfTotal.withColumn(delimiter, functions.size(functions.split(functions.col("*"), delimiter))).count();
            delimiterCounts.put(delimiter, count);
        }

        System.out.println("Delimter Count");
        System.out.println(delimiterCounts);
        System.out.println("Delimter Count");

        String delimiter = ",";
        long maxCount = 0;
        for (Map.Entry<String, Long> entry : delimiterCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                delimiter = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return delimiter;

         */
    }


    private String detectLineDelimiter(Dataset<Row> dfTotal) {
        // TODO : this needs work
        return "\n";
    }

    private String sparkMaster() throws ApiException, IOException {
//        ApiClient client = buildService.kubernetesClient();
//        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
//
//        CoreV1Api coreApi = new CoreV1Api(client);
//
//        String namespace = "default"; // replace with the namespace of the pod
//        String podName = "my-pod"; // replace with the name of the pod
//        V1Pod pod = coreApi.readNamespacedPod("podName", "orphea", null);

//        return  "k8s://https://" + pod.getStatus().getHostIP() + ":443"; // or kubernetes.default.svc : https://kubernetes.default.svc:443
//        return  "k8s://https://" + System.getenv("KUBERNETES_SERVICE_HOST")  + ":" + System.getenv("KUBERNETES_SERVICE_PORT"); // or kubernetes.default.svc : https://kubernetes.default.svc:443

        String sparkMasterHost = "local[*]";

        if (System.getenv("SPARK_MASTER") != null) {
            sparkMasterHost = System.getenv("SPARK_MASTER");
        }

        return sparkMasterHost;
    }

    public static String getFileType(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (Objects.equals(contentType, "text/csv")) {
            return "CSV";
        } else if (Objects.equals(contentType, "application/vnd.ms-excel") ||
                Objects.equals(contentType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {

            return "XLS";
        } else {
            return "UNKNOWN";
        }
    }

    public List<String> getExcelSheetNames(MultipartFile excelFile) {
        List<String> sheetNames = new ArrayList<>();
        try {
            Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sheetNames;
    }

    public MultipartFile convertExcelToCsv(MultipartFile multipartFile, String sheetName) throws IOException {
        // Get the input stream from the multipart file
        InputStream inputStream = multipartFile.getInputStream();

        // Create a workbook object from the Excel sheet
        Workbook workbook = WorkbookFactory.create(multipartFile.getInputStream());

        // Get the first sheet from the workbook
        Sheet sheet = workbook.getSheet(sheetName);

        // Iterate through each row and calculate formulas
        StringBuilder csvData = new StringBuilder();
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            for (Cell cell : row) {
                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                String cellValue = "";
                switch (cell.getCellType()) {
                    case NUMERIC:
                        cellValue = String.valueOf(cell.getNumericCellValue());
                        break;
                    case FORMULA:
                        try {
                            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                            CellValue cellEvaluated = evaluator.evaluate(cell);
                            cellValue = String.valueOf(cellEvaluated.getNumberValue());
                        } catch (Exception e){
                            cellValue = String.valueOf("Formula Parse Error");
                            System.out.println("Not able to convert cell :: " + cell.getAddress());
                        }
                        break;
                    case STRING:
                        cellValue = cell.getStringCellValue();
                        break;
                    default:
                        break;
                }
                csvData.append(cellValue).append(",");
            }
            csvData.deleteCharAt(csvData.length() - 1);
            csvData.append("\n");
        }


        // Write the updated data to CSV

//        StringBuilder csvContent = new StringBuilder();
//        for (org.apache.poi.ss.usermodel.Row row : sheet) {
//            for (Cell cell : row) {
//                csvContent.append(cell.toString());
//                if (cell.getColumnIndex() < row.getLastCellNum() - 1) {
//                    csvContent.append(",");
//                }
//            }
//            csvContent.append("\n");
//        }


        // Close the workbook
        workbook.close();

        // Create a byte array from the CSV data
        byte[] csvData1 = csvData.toString().getBytes();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
        String formattedDateTime = now.format(formatter);


        // Create a new multipart file with the CSV data
        int lastDotIndex = multipartFile.getOriginalFilename().lastIndexOf(".");

        return new MockMultipartFile(multipartFile.getOriginalFilename() + ".csv",
                multipartFile.getOriginalFilename().substring(0, lastDotIndex) + "-" + sheetName + "-" + formattedDateTime + ".csv", "text/csv", csvData1);
    }


}