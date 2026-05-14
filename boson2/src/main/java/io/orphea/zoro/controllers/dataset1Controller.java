package io.orphea.zoro.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.Credentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.orphea.bob.library.models.SocketMessage;
import io.orphea.kepler.library.models.ChartConfigModel;
import io.orphea.kepler.library.models.ChartFilterModel;
import io.orphea.kepler.library.models.ChartMetricModel;
import io.orphea.kepler.library.models.ChartsModel;
import io.orphea.kepler.library.repository.ChartsRepository;
import io.orphea.kitab.library.models.*;
import io.orphea.kitab.library.repository.*;
import io.orphea.passport.library.repository.UserRepository;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.JwtService;
import io.orphea.passport.library.service.UserService;
import io.orphea.sharedUtils.BackingFS;
import io.orphea.sharedUtils.KubernetesUtils;
import io.orphea.sharedUtils.Response.OkResponse;
import io.orphea.sharedUtils.Utils;
import io.orphea.synchro.library.repository.PostgresSyncRepository;
import io.orphea.zoro.library.models.*;
import io.orphea.zoro.library.repository.SchemaRepository;
import io.orphea.zoro.library.repository.SparkResultsRepository;
import io.orphea.zoro.library.services.ZoroService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import jakarta.validation.Valid;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static io.orphea.sharedUtils.Redis.*;
import static io.orphea.sharedUtils.storage.OrpheaMinio.MinioConnection;
import static org.apache.spark.sql.functions.*;


@CrossOrigin
@RestController
@RequestMapping("/api/zoro/dataset")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Zoro", description = "This is a Spark data management service.")
public class dataset1Controller {
    private final UserService userService;
    private final JwtService jwtService;
    private final ZoroService zoroService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final DatasetRepository datasetRepository;
    private final FolderRepository folderRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final BranchRepository branchRepository;
    private final ChartsRepository chartsRepository;
    private final SchemaRepository schemaRepository;
    private final PostgresSyncRepository postgresSyncRepository;
    private final DatasetStatsRepository datasetStatsRepository;
    private final SparkResultsRepository sparkResultsRepository;
    private final AuthzService authzService;

    private static HashMap<UUID, JSONObject> lastResponseToSQLConfigDataRequest = new HashMap<>();

    private final OkResponse response = new OkResponse();
    TransactionModel transactionModel;


    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Import branches by datasetId and branch.")
    @GetMapping("/deleteCSV/{datasetId}/{branch}")
    ResponseEntity<Object> deleteCSV(Principal principal,
                                     @PathVariable("datasetId") UUID datasetId,
                                     @PathVariable("branch") String branch) throws Exception {
        try {
            UUID userId = userService.getUser(principal.getName()).id;

            if (!authzService.isEditor(userId, datasetId)) {
                return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
            }

            List<TransactionModel> transactionModels = transactionRepository
                    .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);

            for (TransactionModel model : transactionModels) {
                if (model.getStatus().equals("active")) {
                    return new ResponseEntity<>("There is a transaction already active", HttpStatus.BAD_REQUEST);
                }
            }

            transactionRepository.deleteAll(transactionRepository.findAllByDatasetIdAndBranch(datasetId, branch));

            // Remove Redis cache
            deleteCache("dataset" + datasetId + branch);
            deleteCache("columns" + datasetId + branch);
            deleteCacheWithWildCard("sparkResults" + datasetId + branch + "*");
            deleteCacheWithWildCard("chartData" + datasetId + branch + "*");

            BackingFS.deleteDatasetFiles(datasetId, branch);

            if (branchRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                BranchModel branchModel = branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch);
//                branchModel.setDatasetId(null);
//                branchModel.setBranch(null);
//                branchModel.setType(null);
                branchRepository.delete(branchModel);
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
//          schedulerRepository.deleteAll(schedulerRepository.findAllByDatasetIdAndBranch(datasetId, branch));
//          deleting charts associated with that dataset
//          List<ChartsModel> datasetCharts = chartsRepository.findAllByDatasetIdAndBranch(datasetId, branch);

            // Update kitab dataset also
            DatasetModel datasetModel = datasetRepository.findDatasetModelById(datasetId);
            datasetModel.setUpdatedAt(new Date());
            datasetModel.setUpdatedBy(userId);
            datasetRepository.save(datasetModel);

            FolderModel folderModel = folderRepository.getById(datasetId);
            folderModel.setUpdatedAt(new Date());
            folderModel.setUpdatedBy(userId);
            folderRepository.save(folderModel);

//            transactionModel.setStatus("completed");
//            transactionModel.setFinishedAt(new Date());
//            transactionModel.setFinishedBy(userId);
//            transactionRepository.save(transactionModel);

            return new ResponseEntity<>("CSV Deleted", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Import branches by datasetId and branch.")
    @PostMapping("/import/{datasetId}/{branch}")
    ResponseEntity<Object> uploadCSVdirect(Principal principal,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam("columnSeparator") String columnSeparator,
                                           @RequestParam("mode") String mode,
                                           @RequestParam("sheetName") String sheetName,
                                           @PathVariable("datasetId") UUID datasetId,
                                           @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;
        UUID transactionId = null;

        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        String fileType = ZoroService.getFileType(file);

        if (!fileType.equals("CSV") && !fileType.equals("XLS")) {
            return new ResponseEntity<>("File type is not correct, only csv and excel files allowed to upload in dataset", HttpStatus.BAD_REQUEST);
        }

        if (fileType.equals("XLS")) { // converting excel to csv and uploading first sheet

            try {
                if (Objects.equals(sheetName, "first")) {
                    for (String sheetNameFromExcel : zoroService.getExcelSheetNames(file)) {
                        file = zoroService.convertExcelToCsv(file, sheetNameFromExcel);
                        break;
                    }
                } else {
                    file = zoroService.convertExcelToCsv(file, sheetName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("There was an error uploading you file in dataset.", HttpStatus.BAD_REQUEST);
            }
            }


//        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
//            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
//        }

        if (!file.isEmpty()) {


            List<TransactionModel> transactionModels = transactionRepository
                    .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
            for (TransactionModel model : transactionModels) {
                if (model.getStatus().equals("active")) {
                    return new ResponseEntity<>("There is a transaction already active", HttpStatus.BAD_REQUEST);
                }
            }
            transactionModel = new TransactionModel();
            transactionModel.setBranch(branch);
            transactionModel.setDatasetId(datasetId);
            transactionModel.setTrigger("upload");
            transactionModel.setStatus("active");
            transactionModel.setCreatedBy(userId);
            transactionModel.setCreatedAt(new Date());

            transactionRepository.save(transactionModel);

            InputStreamReader reader;
            ByteArrayInputStream inputStreamOriginal;
            try {
                inputStreamOriginal = new ByteArrayInputStream(file.getBytes());
                reader = new InputStreamReader(inputStreamOriginal, StandardCharsets.UTF_8);
            } catch (Exception error) {
                return new ResponseEntity<>("Unexpected error " + error, HttpStatus.BAD_REQUEST);
            }


            if (Objects.equals(System.getenv("BACKING_FS"), "s3")) {

                InputStream inputStream = IOUtils.toInputStream(IOUtils.toString(reader), StandardCharsets.UTF_8);

                String bucket = "orphea";
                String targetFilePath = "datasets/" + datasetId + "/" + branch + "/" + file.getOriginalFilename();

                MinioClient minioClient = MinioConnection(
                        System.getenv("MINIO_ENDPOINT"),
                        System.getenv("MINIO_ACCESS_KEY"),
                        System.getenv("MINIO_SECRET_KEY"));

                if (mode.equals("overwrite")) {
                    RemoveObjectArgs removeObject = RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetFilePath)
                            .build();
                    minioClient.removeObject(removeObject);
                }

                PutObjectArgs putObject = PutObjectArgs.builder().bucket(bucket).object(targetFilePath).stream(
                                inputStream, -1, 10485760)
                        .build();

                minioClient.putObject(putObject);
            } else if (Objects.equals(System.getenv("BACKING_FS"), "gs")) {

                System.out.println("Encoding of CSV : " + reader.getEncoding());

                Credentials credentials = BackingFS.getGoogleCredentials();

                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "orphea/datasets/" + datasetId + "/" + branch + "/" + file.getOriginalFilename());
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

                // TODO :
                if (mode.equals("overwrite")) {
                    System.out.println("TODO : ");
                }


                // Read the contents of the file into a String
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStreamOriginal, StandardCharsets.ISO_8859_1));
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader1.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                reader.close();

                String content = sb.toString();

//                System.out.println(content);
                System.out.println(file);

                // Convert the String to a byte array using the desired character encoding
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

                storage.create(blobInfo, bytes);

            } else if (Objects.equals(System.getenv("BACKING_FS"), "hdfs")) {

                System.out.println("Encoding of CSV : " + reader.getEncoding());

                if (!Objects.equals(reader.getEncoding(), "UTF8")) {

                    // Read the contents of the file into a String
                    BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStreamOriginal, StandardCharsets.ISO_8859_1));
                    StringBuilder sb = new StringBuilder();

                    String line;
                    while ((line = reader1.readLine()) != null) {
                        sb.append(line).append("\n");
                    }

                    reader.close();

                    String content = sb.toString();
                }

                // Set up the configuration
                Configuration conf = new Configuration();

                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));


                // Get a reference to the filesystem
                FileSystem fs = FileSystem.get(conf);

                Path path = new Path("/orphea/datasets/" + datasetId + "/" + branch + "/" + file.getOriginalFilename());

                // Create an FSDataOutputStream to write to the file
                FSDataOutputStream outputStream = fs.create(path, true); // TODO : append mode also


                // Get the bytes object you want to write to the file
                byte[] bytes = file.getBytes();

                // Write the bytes to the file
                outputStream.write(bytes);

                // Close the output stream
                outputStream.close();

            } else {
                throw new Exception("Error : no backing FS defined");
            }

            transactionModel.setStatus("completed");
            transactionModel.setFinishedAt(new Date());
            transactionModel.setFinishedBy(userId);
            transactionRepository.save(transactionModel);

            if (!branchRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                BranchModel branchModel1 = new BranchModel();

                branchModel1.setDatasetId(datasetId);
                branchModel1.setBranch(branch);
                branchModel1.setType("raw");
                branchModel1.setCreatedBy(userId);
                branchModel1.setCreatedAt(new Date());

                branchRepository.save(branchModel1);
            }

            // Update kitab dataset also
            DatasetModel datasetModel = datasetRepository.findDatasetModelById(datasetId);

            datasetModel.setUpdatedAt(new Date());
            datasetModel.setUpdatedBy(userId);

            datasetRepository.save(datasetModel);

            FolderModel folderModel = folderRepository.getById(datasetId);

            folderModel.setUpdatedAt(new Date());
            folderModel.setUpdatedBy(userId);
            folderRepository.save(folderModel);

            if (!schemaRepository.existsByDatasetIdAndBranch(datasetId, branch)) { // if no schema found then create it in db
                zoroService.createDBSchemaIfNotExists(datasetId, branch);
            }

            return new ResponseEntity<>(response.okResponse("CSV File Uploaded successfully : " + datasetId), HttpStatus.OK);
        }
        return new ResponseEntity<>("CSV File upload failed : " + datasetId, HttpStatus.BAD_REQUEST);
    }


    @Operation(summary = "deleteDatasetFiles files for overwrite by datasetId and branch.")
    @GetMapping("/deleteDatasetFiles/{datasetId}/{branch}")
    ResponseEntity<Object> deleteDatasetFiles(Principal principal,
                                              @PathVariable("datasetId") UUID datasetId,
                                              @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog with id : " + datasetId, HttpStatus.NOT_FOUND);
        }

        if (!branchRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
            return new ResponseEntity<>("Not found " + datasetId + " " + branch, HttpStatus.BAD_REQUEST);
        }

        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            List<DatasetStatsModel> datasetStatsModel = datasetStatsRepository.findAllByDatasetIdAndBranch(datasetId, branch);
            datasetStatsRepository.deleteAll(datasetStatsModel);
        }

        // Remove Redis cache
        deleteCache("dataset" + datasetId + branch);
        deleteCache("columns" + datasetId + branch);
        deleteCacheWithWildCard("sparkResults" + datasetId + branch + "*");
        deleteCacheWithWildCard("chartData" + datasetId + branch + "*");

        BackingFS.deleteDatasetFiles(datasetId, branch);

        return new ResponseEntity<>("Dataset files deleted successfully!", HttpStatus.OK);


    }

    @Operation(summary = "Ignite files upload by datasetId and branch.")
    @PostMapping("/igniteUpload/{datasetId}/{branch}")
    ResponseEntity<Object> igniteFileUpload(Principal principal,
                                            @RequestParam("type") String type,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam("columnSeparator") String columnSeparator,
                                            @RequestParam("mode") String mode,
                                            @PathVariable("datasetId") UUID datasetId,
                                            @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;
        UUID transactionId = null;

        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog with id : " + datasetId, HttpStatus.NOT_FOUND);
        }

        if (!file.isEmpty()) {

//            List<TransactionModel> transactionModels = transactionRepository
//                    .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
//            for (TransactionModel model : transactionModels) {
//                if (model.getStatus().equals("active")) {
//                    return new ResponseEntity<>("There is a transaction already active", HttpStatus.NOT_FOUND);
//                }
//            }


            TransactionModel transactionModel = new TransactionModel();
            transactionModel.setBranch(branch);
            transactionModel.setDatasetId(datasetId);
            transactionModel.setTrigger("ignite");
            transactionModel.setStatus("active");
            transactionModel.setCreatedBy(userId);
            transactionModel.setCreatedAt(new Date());

            transactionRepository.save(transactionModel);

            InputStream inputStream = file.getInputStream();

            if (Objects.equals(System.getenv("BACKING_FS"), "s3")) {

                String bucket = "orphea";
                String targetFilePath = "datasets/" + datasetId + "/" + branch + "/" + file.getOriginalFilename();

                MinioClient minioClient = MinioConnection(
                        System.getenv("MINIO_ENDPOINT"),
                        System.getenv("MINIO_ACCESS_KEY"),
                        System.getenv("MINIO_SECRET_KEY"));

                if (mode.equals("overwrite")) {
                    BackingFS.deleteDatasetFiles(datasetId, branch);
                }

                PutObjectArgs putObject = PutObjectArgs.builder().bucket(bucket).object(targetFilePath).stream(
                                inputStream, -1, 10485760)
                        .build();

                minioClient.putObject(putObject);
            } else if (Objects.equals(System.getenv("BACKING_FS"), "gs")) {

                Credentials credentials = BackingFS.getGoogleCredentials();

                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "orphea/datasets/" + datasetId + "/" + branch + "/" + file.getOriginalFilename());
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

//                if (mode.equals("overwrite")) {
//                    BackingFS.deleteDatasetFiles(datasetId, branch);
//                }

                storage.create(blobInfo, file.getBytes());

            } else if (Objects.equals(System.getenv("BACKING_FS"), "hdfs")) {


//                if (mode.equals("overwrite")) {
//                    BackingFS.deleteDatasetFiles(datasetId, branch);
//                }

                // Set up the configuration
                Configuration conf = new Configuration();

                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));


                // Get a reference to the filesystem
                FileSystem fs = FileSystem.get(conf);

                Path path = new Path("/orphea/datasets/" + datasetId + "/" + branch + "/" + file.getOriginalFilename());

                OutputStream os = fs.create(path);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    os.write(buffer, 0, bytesRead);
                }

                os.close();
                inputStream.close();


            } else {
                throw new Exception("Error : no backing FS defined");
            }

            transactionModel.setStatus("completed");
            transactionModel.setFinishedAt(new Date());
            transactionModel.setFinishedBy(userId);
            transactionRepository.save(transactionModel);


            if (!branchRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                System.out.println("Branch does not exists, so creating it " + datasetId + " " + branch);
                BranchModel branchModel1 = new BranchModel();

                branchModel1.setDatasetId(datasetId);
                branchModel1.setBranch(branch);
                branchModel1.setType(type);
                branchModel1.setCreatedBy(userId);
                branchModel1.setCreatedAt(new Date());

                branchRepository.save(branchModel1);
            } else {
                System.out.println("Branch does exists, so creating it " + datasetId + " " + branch);
            }
            System.out.println("debug 12 ");
            // Update kitab dataset also
            DatasetModel datasetModel = datasetRepository.findDatasetModelById(datasetId);

            datasetModel.setUpdatedAt(new Date());
            datasetModel.setUpdatedBy(userId);

            datasetRepository.save(datasetModel);
            System.out.println("debug 13 ");
            FolderModel folderModel = folderRepository.getById(datasetId);

            folderModel.setUpdatedAt(new Date());
            folderModel.setUpdatedBy(userId);
            folderRepository.save(folderModel);

            if (!schemaRepository.existsByDatasetIdAndBranch(datasetId, branch)) { // if no schema found then create it in db
                zoroService.createDBSchemaIfNotExists(datasetId, branch);
            }

            return new ResponseEntity<>(response.okResponse("Ignite Uploaded successfully : " + datasetId), HttpStatus.OK);
        }
        return new ResponseEntity<>("Ignite upload failed : " + datasetId, HttpStatus.BAD_REQUEST);
    }

//    @Operation(summary = "This endpoint can be used to read datasets")
//    @GetMapping(path = "/{datasetId}/{branch}", produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<Object> readDataset(Principal principal, @PathVariable("datasetId") UUID datasetId,
//                                       @PathVariable("branch") String branch) {
//
//        UUID userId = userService.getUser(principal.getName()).id;
//
//        if (!authzService.isViewer(userId, datasetId)) {
//            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
//        }
//
//        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();
//
//        resourceViewsModel.setResourceId(datasetId);
//        resourceViewsModel.setAction("view");
//        resourceViewsModel.setViewedBy(userId);
//
//        resourceViewsRepository.save(resourceViewsModel);
//
//        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
//            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
//        }
//
//        try {
//
//            Dataset<Row> dfTotal = zoroService.getSparkDF(datasetId, branch, 300);
//
//            Dataset<Row> df = dfTotal.limit(300);
//            String[] columns = df.columns();
//
//            WindowSpec w = Window.orderBy(columns[0]);
//
//            Dataset<Row> dfRow = df.withColumn("key", row_number().over(w));
//
////            List<Object> datasetList = new ArrayList<>();
//
//            for (Row element : dfRow.collectAsList()) {
//
//                String[] fieldNames = element.schema().fieldNames();
//                Seq<String> fieldNamesSeq = JavaConverters.asScalaIteratorConverter(Arrays.asList(fieldNames).iterator()).asScala().toSeq();
//
////                datasetList.add(element.getValuesMap(fieldNamesSeq));
//            }
//
//            return new ResponseEntity<>(dfRow.toJSON().collectAsList().toString(), HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, String> errorTemplate = new HashMap<>();
//            errorTemplate.put("error", "There was an un-expected error while loading dataset.");
//            errorTemplate.put("errorMessage", ExceptionUtils.getMessage(e));
//            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
//        }
//    }


    @Operation(summary = "This endpoint can be used to filter datasets")
    @PostMapping(path = "/{datasetId}/{branch}/filtered", produces = MediaType.APPLICATION_JSON_VALUE)
    Object filterDataset(
            @RequestBody FilterModel filterModel,
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("branch") String branch, Principal principal) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(datasetId);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        boolean existsPostgresSync = postgresSyncRepository.existsByDatasetIdAndBranch(datasetId, branch);

        if (existsPostgresSync) { // TODO : if postgres Sync exists then no point going to spark read first 1000 rows from postgres, its much faster
            System.out.println("YES postgres sync exists for " + datasetId);
        }

        try {
            UUID resultsId = UUID.randomUUID();

//            if (Utils.isSparkInKubernetes()) {
//                runSparkGetDatasetJobWithKubernetes(userId, datasetId, branch, filterModel, resultsId);
//            } else {
                String result = getDataset(datasetId, branch, filterModel, resultsId);
//            }

//            System.out.println(result);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "This endpoint can be used to column stats of datasets")
    @PostMapping(path = "/columnStats", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> columnStats(Principal principal, @Valid @RequestBody ColumnStatsModel columnStatsModel) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, columnStatsModel.getDatasetId())) {
            return new ResponseEntity<>("Access Denied to " + columnStatsModel.getDatasetId(), HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(columnStatsModel.getDatasetId());
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!datasetRepository.existsById(columnStatsModel.getDatasetId())) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + columnStatsModel.getDatasetId(), HttpStatus.NOT_FOUND);
        }

        String jsonResults = getCache("sparkResults" + columnStatsModel.getDatasetId() + columnStatsModel.getBranch() + columnStatsModel.getColumn());
        if (jsonResults != null) {
            return new ResponseEntity<>(jsonResults, HttpStatus.OK);
        }
        String sparkResultsCache = getCache("sparkResults" + columnStatsModel.getDatasetId() + columnStatsModel.getBranch() + columnStatsModel.getColumn());

        if(sparkResultsCache != null){
            return ResponseEntity.ok(sparkResultsCache);
        }

        try {
            UUID resultsId = UUID.randomUUID();

            if (Utils.isSparkInKubernetes()) {
                runSparkColumnStatsJobWithKubernetes(userId, columnStatsModel, resultsId);
            } else {
                calculateStatistics(columnStatsModel, resultsId);
            }
            return ResponseEntity.ok(resultsId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex);
        }
    }

    @Operation(summary = "This endpoint is for spark pods to send data")
    @PostMapping(path = "/sparkResults", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> postSparkResults(Principal principal, @Valid @RequestBody SparkResults sparkResults) throws JsonProcessingException {

//        ObjectMapper objectMapper = new ObjectMapper();
//        String respData = objectMapper.writeValueAsString(sparkResults.getResults());

//        setCache("sparkResults" + sparkResults.getDatasetId() + sparkResults.getBranch() + sparkResults.getColumnName(), respData, null);

//        sparkResultsRepository.save(sparkResults);

        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("success");

        template.convertAndSend("/topic/sparkResults/" + sparkResults.getId(), textMessage);

        return new ResponseEntity<>("Spark Results stored and socket notified", HttpStatus.OK);

    }

    @Operation(summary = "This endpoint is for frontend to get spark results after socket notification.")
    @GetMapping(path = "/{Id}/sparkResults", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getSparkResults(Principal principal, @PathVariable("Id") UUID Id) {

        if (sparkResultsRepository.existsById(Id)) {
            SparkResults sparkResults = sparkResultsRepository.findById(Id).get();

            sparkResultsRepository.delete(sparkResults);

//            return new ResponseEntity<>(sparkResults.getResults(), HttpStatus.OK);
            return new ResponseEntity<>("sparkResults.getResults()", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No Spark Results found for the give Id " + Id, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get all files by datasetId and branch.")
    @GetMapping("/files/{datasetId}/{branch}")
    ResponseEntity<Object> datasetFiles(Principal principal,
                                        @PathVariable("datasetId") UUID datasetId,
                                        @PathVariable("branch") String branch)
            throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;


        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }


        // check if the dataset exists in catalog
        if (!datasetRepository.existsById(datasetId)) {
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }


        List<Map<String, Object>> rows = BackingFS.getListOfFiles(datasetId, branch);

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


        return new ResponseEntity<>(files, HttpStatus.OK);

    }

    public static String columnDataType(Dataset<Row> dataset, String colName) {
        StructType schema = dataset.schema();
        return schema.apply(colName).dataType().typeName();
    }

    static boolean isInvalidQuery(QueryConfigRequest query) {
        return query.getDatasetId() == null ||
                query.getBranch() == null ||
                query.getMetric() == null ||
                query.getMetric().size() == 0;
    }

    @Operation(summary = "Get unique values for a column")
    @PostMapping("/uniqueColumnValues")
    public ResponseEntity<Object> uniqueColumnValues(Principal principal, @RequestBody List<ColumnModel> columnModelList) throws Exception {
        Connection connection = null;
        Statement stmt = null;
        HashMap<String, ArrayList<Object>> result = new HashMap<>();
        try {
            // Check if Dataset Exists by ID and branch
            for (ColumnModel columnModel : columnModelList) {
                if (!folderRepository.existsById(columnModel.getDatasetID())) {
                    return new ResponseEntity<>("Dataset with Id " + columnModel.getDatasetID().toString() + " does not exist", HttpStatus.NOT_FOUND);
                }
                Dataset<Row> sparkData = zoroService.getSparkDF(columnModel.getDatasetID(), columnModel.getBranch(), -1);
                if (columnModel.getType().equals("value")) {
                    Dataset<Row> groupedDataset = sparkData.select(columnModel.getColumn()).distinct();

                    ArrayList<Object> res = new ArrayList<>();

                    for (Row r : groupedDataset.collectAsList()) {
                        res.add(r.getAs(columnModel.getColumn()));
                    }
                    result.put(columnModel.getDatasetID() + columnModel.getColumn() + columnModel.getBranch() + columnModel.getType(), res);
                }
                else if (columnModel.getType().equals("range")) {
                    Object minVal = sparkData.agg(functions.min(columnModel.getColumn())).first().get(0);
                    Object maxVal = sparkData.agg(functions.max(columnModel.getColumn())).first().get(0);
                    ArrayList<Object> res = new ArrayList<>();
                    JSONObject obj = new JSONObject();
                    obj.put("minVal" , minVal);
                    obj.put("maxVal" , maxVal);
//                    res.add(obj);
                    res.add(minVal);
                    res.add(maxVal);
                    result.put(columnModel.getDatasetID() + columnModel.getColumn() + columnModel.getBranch() + columnModel.getType(), res);
                }

            }

            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get data from postgres.")
    @PostMapping("/postgresData")
    public ResponseEntity<Object> PostgresSyncDatasetIdBranchDelete(Principal principal,
                                                                    @RequestBody String query
    ) throws Exception {
        // TODO : delete tables also cascade properly.

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + System.getenv("DB_HOST") + ":5432/kepler",
                            System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));

            stmt = connection.createStatement();
            ResultSet rs1 = stmt.executeQuery(query);

//            ArrayList<String> s = new ArrayList<>();
//            s.add("SUM(price)");
//            s.add("AVG(price)");
//            System.out.println(generateDimensionObject(rs1, s));

            ResultSet result = stmt.executeQuery(query);
            JSONArray json = convertResultSetIntoJSON(result);

            stmt.close();
            connection.close();

//
//            if (folderRepository.existsById(UUID.fromString("d81d064b-5708-413c-84e3-6394d6d468f6"))) {
////                return new ResponseEntity<>("Dataset with Id " + "d81d064b-5708-413c-84e3-6394d6d468f6" + " does not exist", HttpStatus.NOT_FOUND);
//                System.out.println("Found Dataset!!");
//            }
//
//            if (postgresSyncRepository.existsByDatasetIdAndBranch(UUID. fromString("d81d064b-5708-413c-84e3-6394d6d468f6"), "master")) {
//                System.out.println("Found Sync !!!");
//                System.out.println(postgresSyncRepository.findByDatasetIdAndBranch(UUID. fromString("d81d064b-5708-413c-84e3-6394d6d468f6"), "master").getTableName());
//
////                return new ResponseEntity<>("Postgres sync does not exists for the datasetId " + "d81d064b-5708-413c-84e3-6394d6d468f6" + " and branch." + "master", HttpStatus.NOT_FOUND);
//
//            }
//
////            return new ResponseEntity<>(postgresSyncRepository.findByDatasetIdAndBranch(datasetId, branch), HttpStatus.OK);

            return new ResponseEntity<>(json, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get data using config query")
    @PostMapping("/sqlConfigData")
    public ResponseEntity<Object> DataBySqlConfig(Principal principal, @RequestBody QueryConfigRequest query) throws Exception {
        if (isInvalidQuery(query)) {
            return new ResponseEntity<>("Add columns to get Data", HttpStatus.BAD_REQUEST);
        }
        UUID userId = userService.getUser(principal.getName()).id;

        // Check if Dataset Exists
        if (!folderRepository.existsById(query.getDatasetId())) {
            return new ResponseEntity<>("Dataset with Id " + query.getDatasetId().toString() + " does not exist", HttpStatus.NOT_FOUND);
        }

        try {
            lastResponseToSQLConfigDataRequest.remove(query.getChartUUID());

            JSONObject j = getDataFromSQL(query);

            lastResponseToSQLConfigDataRequest.put(query.getChartUUID(), j);

            // Sending to socket to tell that someone opened chart
//            SocketMessage textMessage = new SocketMessage();
//            textMessage.setMessage(userId.toString());
//
//            System.out.println("sending chart socket on :: " + j);
//            System.out.println("sending chart socket on :: " + query.getChartUUID());
//
//            template.convertAndSend("/topic/chart/" + query.getChartUUID(), textMessage);

            return new ResponseEntity<>(j, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "This can be used to get multiple chart data at once.")
    @PostMapping("/getChartDataByIds")
    public ResponseEntity<Object> getChartDataByIds(Principal principal, @RequestBody ChartDataByIdsRequest chartDataByIdsRequest ) {

        UUID userId = userService.getUser(principal.getName()).id;




        try {
            List<UUID> IDs = chartDataByIdsRequest.getChartIds();
            System.out.println(chartDataByIdsRequest.getFilters().toString());
            JSONArray chartdata = new JSONArray();

            for (UUID id : IDs) {

                if (!chartsRepository.existsById(id)) {
                    return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
                }


                JSONObject obj = new JSONObject();

                ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

                resourceViewsModel.setResourceId(id);
                resourceViewsModel.setAction("view");
                resourceViewsModel.setViewedBy(userId);

                resourceViewsRepository.save(resourceViewsModel);

                Optional<ChartsModel> chart = chartsRepository.findById(id);
                QueryConfigRequest queryConfigRequest = null;
                if (chart.isPresent()) {
                    queryConfigRequest = converterOne(id, chart.get().getChartConfig());
                }

                // Check if Dataset Exists
                if (!folderRepository.existsById(queryConfigRequest.getDatasetId())) {
                    return new ResponseEntity<>("Dataset with Id " + queryConfigRequest.getDatasetId().toString() + " does not exist", HttpStatus.NOT_FOUND);
                }

                obj.put("chartState", chart);

                if (!authzService.isViewer(userId, id)) {
                    obj.put("error", "Error : Access Denied");
                }
                else {
                    if(chartDataByIdsRequest.getFetchCachedData()){
                        queryConfigRequest.setFetchCachedData(true);
                    }
                    JSONObject js = getDataFromSQL(queryConfigRequest);
                    obj.put("chartData", js);
                }

                chartdata.add(obj);
            }

            return new ResponseEntity<>(chartdata, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private static JSONObject convertToChartData(Dataset<Row> resultSet, ArrayList<String> metrics, QueryConfigRequest queryConfigRequest) throws Exception {
        HashMap<String, ArrayList<Object>> data = new HashMap<>();
        ArrayList<String> dimensionSeries = new ArrayList<>();

        Boolean hasDimensions = queryConfigRequest.getDimension() != null;
        Seq<String> dimensionColumns = null;
        if(hasDimensions) {
            dimensionColumns = JavaConverters.asScalaIteratorConverter(queryConfigRequest.getDimension().iterator()).asScala().toSeq();
        }

        resultSet = resultSet.sort(queryConfigRequest.getDimension().get(0)); // default order by asc names of x-axis
//        resultSet = resultSet.orderBy("avg(" + queryConfigRequest.getMetric().get(0).getColumnName() + ")"); // default order by asc


        for(Row r: resultSet.collectAsList()) {
            if(hasDimensions) {
                scala.collection.Map<String, Object> mp = r.getValuesMap(dimensionColumns);
                scala.collection.Iterator<Object> iterator = mp.valuesIterator();

                StringBuilder builder = new StringBuilder(String.valueOf(iterator.next()));
                while (iterator.hasNext()) {
                    builder.append(", ").append(iterator.next());
                }
                dimensionSeries.add(builder.toString());
            }

            for(String metric: metrics) {
                data.computeIfAbsent(metric, k -> new ArrayList<>());
                data.get(metric).add(r.getAs(metric));
            }
        }

        JSONObject js = new JSONObject();

        js.put("data", data);
        js.put("dimensions", dimensionSeries);

        return js;
    }

    private JSONObject convertToChartDataTimeSeries(Dataset<Row> resultSet, ArrayList<String> metrics, QueryConfigRequest queryConfigRequest) throws Exception {
        ArrayList<String> dimensionsArray = new ArrayList<>();
        String[] columns = resultSet.columns();
        int total_columns = columns.length;

        String timeColumn = queryConfigRequest.getTime().getTimeColumn();
        String timeGrain =  queryConfigRequest.getTime().getTimeGrain();
        String timeRange =  queryConfigRequest.getTime().getTimeRange();

        HashMap<String, ArrayList<Object>> timeSeriesObject = new HashMap<>();

        Boolean hasDimensions = queryConfigRequest.getDimension() != null;
        Seq<String> dimensionColumns = null;
        if(hasDimensions) {
            dimensionColumns = JavaConverters.asScalaIteratorConverter(queryConfigRequest.getDimension().iterator()).asScala().toSeq();
        }

        if(timeGrain == null) {
            timeGrain = "day";
        }

        ArrayList<String> dim = new ArrayList<>();
        dim.add(timeColumn);

        if (!Objects.equals(resultSet.select(col(timeColumn)).schema().fields()[0].dataType().typeName(), "timestamp")) {
            throw new Exception("Time Column is of not type \"timestamp\"");
        }

        ArrayList<Object> timeSeries = new ArrayList<>();

        resultSet = resultSet.orderBy(timeColumn); // default order by asc

//        resultSet= resultSet.groupBy(month(col(timeColumn)).alias(timeColumn))
//                    .agg(count("*").alias(timeColumn));

        for (Row row : resultSet.collectAsList()) {
            LocalDateTime t = ((java.sql.Timestamp) row.getAs(timeColumn)).toLocalDateTime();
            long milis = t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if(timeRange != null) {
                String[] range = timeRange.split(",");
                if(milis < Long.parseLong(range[0]) || milis > Long.parseLong(range[1]))
                    continue;
            }

            TemporalField wom = WeekFields.of(Locale.getDefault()).weekOfYear();

            String dayOfWeek = t.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale(queryConfigRequest.getUserLocale()));
            String month = t.getMonth().getDisplayName(TextStyle.SHORT, new Locale(queryConfigRequest.getUserLocale()));

            String s = "";
            switch(timeGrain) {
                case "year":
                    s = String.format("%s", t.getYear());
                    break;
                case "quarter":
                    s = String.format("%s Q%s", (t.getMonthValue() / 4) + 1, t.getYear());
                    break;
                case "date":
                    s = String.format("%s %s %s", month, t.getDayOfMonth(), t.getYear());
                    break;
                case "day":
                    s = String.format("%s W%s %s", t.getYear(), t.get(wom), dayOfWeek);
                    break;
                case "week":
                    s = String.format("%s W%s", t.getYear(),  t.get(wom));
                    break;
                case "month":
                    s = String.format("%s-%s", t.getYear(), month);
                    break;
                case "hour":
                    s = String.format("%s-%s-%s %s h", t.getYear(), month, t.getDayOfMonth(), t.getHour());
                    break;
                case "minute":
                    s = String.format("%s-%s-%s %s:%s", t.getYear(), month, t.getDayOfMonth(), t.getHour(), t.getMinute());
                    break;
                case "second":
                    s = String.format("%s-%s-%s %s:%s:%s", t.getYear(), t.getMonth(), month, t.getHour(), t.getMinute(), t.getSecond());
                    break;

            }

            dimensionsArray.add(s);

            StringBuilder builder = new StringBuilder();
            if(hasDimensions) {
                scala.collection.Map<String, Object> mp = row.getValuesMap(dimensionColumns);
                scala.collection.Iterator<Object> iterator = mp.valuesIterator();

                builder = new StringBuilder();
                while (iterator.hasNext()) {
                    builder.append(", ").append(iterator.next());
                }
            }

            for(String metric: metrics) {
                String key = hasDimensions ? metric + builder.toString(): metric;
                timeSeriesObject.computeIfAbsent(key, k -> new ArrayList<>());
                timeSeriesObject.get(key).add(row.getAs(metric));
            }
        }

        JSONObject js = new JSONObject();
        js.put("data", timeSeriesObject);
        js.put("dimensions", dimensionsArray);

        return js;
    }

    public Dataset<Row> getSparkDataframe(QueryConfigRequest query) throws Exception {
//        TODO: ADD ROW LIMIT HERE WHEN FETCHING THE SPARK DATA FRAME
        Dataset<Row> sparkData = zoroService.getSparkDF(query.getDatasetId(), query.getBranch(), -1);
        RelationalGroupedDataset groupedDataset = null;

        if (query.getFilter() != null) {
            for (int i = 0; i < query.getFilter().size(); i++) {
                String columnName = query.getFilter().get(i).getColumnName();
                Column column = sparkData.col(columnName);

                String operator = query.getFilter().get(i).getOperator();
                String value = query.getFilter().get(i).getFilterValue();

                String[] v = value.split(",");

                switch (operator) {
                    case "equal":
                        sparkData = sparkData.where(column.equalTo(value));
                        break;
                    case "notEqual":
                        sparkData = sparkData.where(column.notEqual(value));
                        break;
                    case "lessThan":
                        sparkData = sparkData.where(column.lt(value));
                        break;
                    case "lessThanEqual":
                        sparkData = sparkData.where(column.leq(value));
                        break;
                    case "greaterThan":
                        sparkData = sparkData.where(column.gt(value));
                        break;
                    case "greaterThanEqual":
                        sparkData = sparkData.where(column.geq(value));
                        break;
                    case "in":
                        sparkData = sparkData.where(column.isin((Object[]) v));
                        break;
                    case "like":
                        sparkData = sparkData.where(column.like(value));
                        break;
                    case "isNotNull":
                        sparkData = sparkData.where(column.isNotNull());
                        break;
                    case "isNull":
                        sparkData = sparkData.where(column.isNull());
                        break;
                }
            }
        }

        if ((query.getDimension() != null && query.getDimension().size() > 0) || (query.getTime() != null && query.getTime().getTimeColumn() != null)) {
            ArrayList<Column> cols = new ArrayList<>();
            for (String s : query.getDimension())
                cols.add(col(s));
            if (query.getTime() != null && query.getTime().getTimeColumn() != null)
                cols.add(col(query.getTime().getTimeColumn()));

            Column[] colArray = cols.toArray(new Column[0]);
            groupedDataset = sparkData.groupBy(colArray);
        }

        if (query.getMetric() != null) {
            HashMap<String, String> metricMap = new HashMap<>();

            List<Tuple2<String, String>> seq = new ArrayList<>();

            Tuple2<String, String> t2 = null;
            for (MetricModel metric : query.getMetric()) {
                String columnName = metric.getColumnName();
                String aggregateFunction = metric.getAggregate();
                metricMap.put(columnName, aggregateFunction);

                t2 = new Tuple2<String, String>(columnName, aggregateFunction);
                seq.add(t2);
            }

            if (groupedDataset != null) {
                if(t2 != null) {
                    seq.remove(seq.size() - 1);
                    Seq<Tuple2<String, String>> s = JavaConverters.asScalaIteratorConverter(seq.iterator()).asScala().toSeq();
                    sparkData = groupedDataset.agg(t2, s);
                }
            }
            else{
                if(t2 != null) {
                    seq.remove(seq.size() - 1);
                    Seq<Tuple2<String, String>> s = JavaConverters.asScalaIteratorConverter(seq.iterator()).asScala().toSeq();
                    sparkData = sparkData.agg(t2, s);
                }
            }
        }

        if (query.getMetric() == null && groupedDataset != null) {
            sparkData = groupedDataset.df();
        }

        return sparkData;
    }

    static String generateColumnName(String column, String aggregate) {
        if (column != null && aggregate != null)
            return String.format("%s(%s)", aggregate, column);
        return null;
    }

    // HELPERS ========================================================================================================
    public JSONObject getDataFromSQL(QueryConfigRequest queryConfigRequest) throws Exception {

        // Get Redis Cache, if it is there
        if(queryConfigRequest.getChartUUID() != null && queryConfigRequest.isFetchCachedData()) {
            String datasetCache = getCache("chartData" + queryConfigRequest.getDatasetId() + queryConfigRequest.getBranch() + queryConfigRequest.getChartUUID());
            System.out.println("getting Chart Cache :: " + "chartData" + queryConfigRequest.getDatasetId() + queryConfigRequest.getBranch() + queryConfigRequest.getChartUUID());
            if(datasetCache != null) {
                JSONParser parser = new JSONParser();
                JSONObject j = (JSONObject) parser.parse(datasetCache);
                j.put("cachedData", true);
                return j;
            }
            System.out.println("did not find Chart Cache :: " + "chartData" + queryConfigRequest.getDatasetId() + queryConfigRequest.getBranch() + queryConfigRequest.getChartUUID());
        }

        Dataset<Row> sparkData = getSparkDataframe(queryConfigRequest);
        boolean flagTrimmedData = false;

        if (queryConfigRequest.getRowSelect() > -1) {
            sparkData = sparkData.limit(queryConfigRequest.getRowSelect());
        } else {
            flagTrimmedData = sparkData.count() > 50000;
            sparkData = sparkData.limit(50000);
        }


        // CREATE METRIC ARRAY
        ArrayList<String> metrics = new ArrayList<>();
        for (MetricModel mm: queryConfigRequest.getMetric()) {
            metrics.add(generateColumnName(mm.getColumnName(), mm.getAggregate()));
        }

        JSONObject json2 = null;

        if(queryConfigRequest.getChartId().equals("table")) {
            json2 = convertToTableData(sparkData, queryConfigRequest, metrics);
            return json2;
        }

        // THIS IS CREATING DIMENSION RESULT SET
        Dataset<Row> dimensionResultSet = null;

        if (queryConfigRequest.getTime() != null && queryConfigRequest.getTime().getTimeColumn() != null) {
            json2 = convertToChartDataTimeSeries(sparkData, metrics, queryConfigRequest);
        } else {
            json2 = convertToChartData(sparkData, metrics, queryConfigRequest);
        }

        JSONObject json = new JSONObject();

        json.put("data", json2.get("data"));
        json.put("dimensions", json2.get("dimensions"));
        json.put("metrics", metrics);
        json.put("rows", sparkData.count());
        json.put("trimmedData", flagTrimmedData);

        return json;
    }


    private JSONObject convertToTableData(Dataset<Row> sparkData, QueryConfigRequest queryConfigRequest, ArrayList<String> metrics) {
        JSONObject tableData = new JSONObject();
        StructField[] fields = sparkData.schema().fields();

        JSONArray columns = new JSONArray();
        JSONArray resultantDataArray = new JSONArray();
        ArrayList<String> colList = new ArrayList<String>(Arrays.asList(sparkData.columns()));
        Seq<String> colSeq = JavaConverters.asScalaIteratorConverter(colList.iterator()).asScala().toSeq();

        for(int i = 0; i< fields.length; i++) {
            JSONObject colObj = new JSONObject();
            colObj.put("name", fields[i].name());
            colObj.put("type", fields[i].dataType().typeName());
            if(metrics.contains(fields[i].name())) {
                for(Row r: sparkData.agg(max(fields[i].name())).collectAsList()) {
                    colObj.put("max", r.get(0));
                }
            }
            columns.add(colObj);
        }

        for(Row r: sparkData.collectAsList()) {
            scala.collection.immutable.Map<String, Object> mp = r.getValuesMap(colSeq);
            Map<String, Object> mapObj = JavaConverters.mapAsJavaMapConverter(mp).asJava();
            resultantDataArray.add(new JSONObject(mapObj));
        }



        tableData.put("columns", columns);
        tableData.put("data", resultantDataArray);
        tableData.put("rows", sparkData.count());
        tableData.put("metrics", metrics);

        return tableData;
    }

    private static JSONArray convertResultSetIntoJSON(ResultSet resultSet) throws Exception {
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

    private static QueryConfigRequest converterOne(UUID chartUUID, ChartConfigModel chartConfigModel) {
        QueryConfigRequest request = new QueryConfigRequest();

        request.setChartUUID(chartUUID);
        request.setDatasetId(chartConfigModel.getDatasetId());
        request.setChartId(chartConfigModel.getChartId());
        request.setBranch(chartConfigModel.getBranch());
        request.setRowSelect(chartConfigModel.getRowSelect());

        TimeRequest time = new TimeRequest();
        if (chartConfigModel.getTime() != null) {
            time.setTimeColumn(chartConfigModel.getTime().getTimeColumn());
            time.setTimeGrain(chartConfigModel.getTime().getTimeGrain());
            time.setTimeRange(chartConfigModel.getTime().getTimeRange());
        }
        request.setTime(time);

        List<String> dimensionsList = new ArrayList<>();
        dimensionsList.addAll(chartConfigModel.getDimension());
        request.setDimension(dimensionsList);

        List<SqlFilterModel> filterModelList = new ArrayList<>();
        for (ChartFilterModel filter : chartConfigModel.getFilter()) {
            SqlFilterModel s = new SqlFilterModel();
            s.setOperator(filter.getOperator());
            s.setFilterValue(filter.getFilterValue());
            s.setOperator(filter.getOperator());
        }
        request.setFilter(filterModelList);

        List<MetricModel> metricModelList = new ArrayList<>();
        for (ChartMetricModel metric : chartConfigModel.getMetric()) {
            MetricModel s = new MetricModel();
            s.setAggregate(metric.getAggregate());
            s.setColumnName(metric.getColumnName());
            metricModelList.add(s);
        }
        request.setMetric(metricModelList);

        List<MetricModel> sortByList = new ArrayList<>();
        for (ChartMetricModel metric : chartConfigModel.getSortBy()) {
            MetricModel s = new MetricModel();
            s.setAggregate(metric.getAggregate());
            s.setColumnName(metric.getColumnName());
            sortByList.add(s);
        }
        request.setSortBy(sortByList);

        return request;
    }

    @Async
    public void calculateStatistics(ColumnStatsModel columnStatsModel, UUID resultsId) throws Exception {

        Dataset<Row> dfTotal = zoroService.getSparkDF(columnStatsModel.getDatasetId(), columnStatsModel.getBranch(), -1);

        Map<String, Object> counts = new HashMap<>();

        Dataset<Row> lengths = null;
        Dataset<Row> distribution = null;


        if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "string")) {

            Dataset<Row> dfCases = dfTotal.withColumn(columnStatsModel.getColumn(),
                    when(
                            col(columnStatsModel.getColumn()).rlike("^[A-Z ]*$"), "UPPER")
                            .when(
                                    col(columnStatsModel.getColumn()).rlike("^[a-z ]*$"), "LOWER")
                            .when(
                                    col(columnStatsModel.getColumn()).rlike("^[a-zA-Z ]*$"), "MIXED")
                            .otherwise("Alphanumeric")
            );
            dfCases = dfCases.select(col(columnStatsModel.getColumn()));

            counts.put("MixedCase", dfCases.filter(col(columnStatsModel.getColumn()).contains("MIXED")).count());
            counts.put("Lowercase", dfCases.filter(col(columnStatsModel.getColumn()).contains("LOWER")).count());
            counts.put("Uppercase", dfCases.filter(col(columnStatsModel.getColumn()).contains("UPPER")).count());
            counts.put("Alphanumeric", dfCases.filter(col(columnStatsModel.getColumn()).contains("Alphanumeric")).count());


            counts.put("Numeric", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).cast("int").isNotNull()).count());

            counts.put("Empty", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).isNaN()).count());

            // Create a column with the length of each value in the specified column
            Column lengthCol = functions.length(dfTotal.col(columnStatsModel.getColumn()));

            // Create a column with the trimmed version of each value in the specified column
            Column trimmedCol = functions.trim(dfTotal.col(columnStatsModel.getColumn()));

            // Create a column with a boolean value indicating whether each value in the specified column needs to be trimmed
            Column needsTrimCol = lengthCol.gt(functions.length(trimmedCol));

            // Count the number of values in the specified column that need to be trimmed
            long needsTrimCount = dfTotal.filter(needsTrimCol).count();


            counts.put("Needs Trim", needsTrimCount);

            distribution = dfTotal
                    .groupBy(columnStatsModel.getColumn())
                    .count()
                    .sort(desc("count"))
                    .limit(300);

            distribution = distribution
                    .withColumn("percentage", distribution.col("count").divide(distribution.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed(columnStatsModel.getColumn(), "name");


            lengths = dfTotal
                    .withColumn("length", length(dfTotal.col(columnStatsModel.getColumn())))
                    .groupBy("length")
                    .count()
                    .orderBy("length")
                    .limit(300);

            lengths = lengths
                    .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed("length", "name");


        } else if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "integer") || Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "bigint") || Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "double")) {

            counts.put("Min", dfTotal.agg(min(dfTotal.col(columnStatsModel.getColumn()))).first().get(0));
            counts.put("Max", dfTotal.agg(max(dfTotal.col(columnStatsModel.getColumn()))).first().get(0));

            counts.put("Mean", Math.round((Double) dfTotal.select(mean(columnStatsModel.getColumn())).first().get(0)));
            counts.put("Standard Deviation", Math.round((Double) dfTotal.select(stddev(columnStatsModel.getColumn())).first().get(0)));

            counts.put("Empty", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).isNaN()).count());

            counts.put("Negatives", dfTotal.agg(sum(functions.when(dfTotal.col(columnStatsModel.getColumn()).lt(0), 1).otherwise(0))).first().getLong(0));
            counts.put("Positives", dfTotal.agg(sum(functions.when(dfTotal.col(columnStatsModel.getColumn()).gt(0), 1).otherwise(0))).first().getLong(0));


            distribution = dfTotal
                    .groupBy(columnStatsModel.getColumn())
                    .count()
                    .sort(desc("count"))
                    .limit(300);

            distribution = distribution
                    .withColumn("percentage", distribution.col("count").divide(distribution.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed(columnStatsModel.getColumn(), "name");


            lengths = dfTotal
                    .withColumn("length", length(dfTotal.col(columnStatsModel.getColumn())))
                    .groupBy("length")
                    .count()
                    .orderBy("length")
                    .limit(300);

            lengths = lengths
                    .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed("length", "name");

        } else if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "date") || Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "timestamp")) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

            counts.put("Min", sdf.format(dfTotal.agg(min(dfTotal.col(columnStatsModel.getColumn()))).first().get(0)));
            counts.put("Max", sdf.format(dfTotal.agg(max(dfTotal.col(columnStatsModel.getColumn()))).first().get(0)));

            lengths = dfTotal.withColumn("name", date_format(dfTotal.col(columnStatsModel.getColumn()), "EEEE")).groupBy("name").count();

            lengths = lengths
                    .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed("length", "name");

//                    counts.put( dfTotal.withColumn("year", year(dfTotal.col(columnStatsModel.getColumn()))).groupBy("year").count();
            distribution = dfTotal.withColumn("name", month(dfTotal.col(columnStatsModel.getColumn()))).groupBy("name").count();
//                    distribution = dfTotal.withColumn("name", days(dfTotal.col(columnStatsModel.getColumn()))).groupBy("name").count();

        } else if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "boolean")) {

            // Count the number of true values in the boolean column
            long trueCount = dfTotal.filter(col(columnStatsModel.getColumn()).equalTo(true)).count();

            // Count the number of false values in the boolean column
            long falseCount = dfTotal.filter(col(columnStatsModel.getColumn()).equalTo(false)).count();

            counts.put("True", trueCount);
            counts.put("False", falseCount);

        }

        counts.put("Distinct", dfTotal.agg(functions.countDistinct(columnStatsModel.getColumn())).first().get(0));

        counts.put("Rows", dfTotal.count());
        counts.put("Columns", dfTotal.columns().length);
        counts.put("Nulls", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).isNull()).count());

        HashMap<String, Object> result = new HashMap<>();

        List<Map<String, Object>> distributionRows = new ArrayList<>();

        if (distribution != null) {
            distributionRows = distribution
                    .collectAsList()
                    .stream()
                    .map(row -> {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (String fieldName : row.schema().fieldNames()) {
                            rowMap.put(fieldName, row.getAs(fieldName));
                        }
                        return rowMap;
                    })
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> lengthRows = new ArrayList<>();

        if (lengths != null) {
            lengthRows = lengths
                    .collectAsList()
                    .stream()
                    .map(row -> {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (String fieldName : row.schema().fieldNames()) {
                            rowMap.put(fieldName, row.getAs(fieldName));
                        }
                        return rowMap;
                    })
                    .collect(Collectors.toList());
        }


        result.put("columnDataType", columnDataType(dfTotal, columnStatsModel.getColumn()));
        result.put("distribution", distributionRows);
        result.put("lengths", lengthRows);
        result.put("counts", counts);

        SparkResults sparkResults = new SparkResults();

        sparkResults.setId(resultsId);
//        sparkResults.setResults(result);

        sparkResultsRepository.save(sparkResults);


        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("success");

        template.convertAndSend("/topic/sparkResults/" + sparkResults.getId(), textMessage);

    }

    @Async
    private void runSparkColumnStatsJobWithKubernetes(UUID userId, ColumnStatsModel columnStatsModel, UUID resultsId) throws Exception {
        HashMap<String, String> envVars = new HashMap<>();
        envVars.put("DATASET_ID", String.valueOf(columnStatsModel.getDatasetId()));
        envVars.put("BRANCH", columnStatsModel.getBranch());
        envVars.put("COLUMN", columnStatsModel.getColumn());
        envVars.put("RESULTS_ID", String.valueOf(resultsId));
        envVars.put("BUILD_ID", String.valueOf(resultsId));

        long millisInOneDay = 12 * 60 * 60 * 1000L;
        Date expiresIn = new Date(System.currentTimeMillis() + millisInOneDay);

        var user = userRepository.findById(userId)
                .orElseThrow();
        var access_token = jwtService.generateToken(user);

        envVars.put("ACCESS_TOKEN", access_token);

        new KubernetesUtils().sparkWithKubernetes(envVars, "io.orphea.ColumnStats","columnstats",  1, "512M", 1, 0);
    }

    private void runSparkGetDatasetJobWithKubernetes(UUID userId, UUID datasetId, String branch, FilterModel filterModel, UUID resultsId) throws Exception {
        HashMap<String, String> envVars = new HashMap<>();
        envVars.put("DATASET_ID", String.valueOf(datasetId));
        envVars.put("BRANCH", branch);
        envVars.put("RESULTS_ID", String.valueOf(resultsId));
        envVars.put("BUILD_ID", String.valueOf(resultsId));

//        byte[] modelBytes = null;
//        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
//             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
//            oos.writeObject(filterModel);
//            modelBytes = bos.toByteArray();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(filterModel);

        String filterModelBase64 = Base64.getEncoder().encodeToString(json.getBytes());

        envVars.put("FILTER_BASE64", filterModelBase64);


        long millisInOneDay = 12 * 60 * 60 * 1000L;
        Date expiresIn = new Date(System.currentTimeMillis() + millisInOneDay);

        var user = userRepository.findById(userId)
                .orElseThrow();
        var access_token = jwtService.generateToken(user);

        envVars.put("ACCESS_TOKEN", access_token);

        new KubernetesUtils().sparkWithKubernetes(envVars, "io.orphea.Dataset","dataset",  1, "512M", 1, 0);
    }

    public String getDataset(UUID datasetId, String branch, FilterModel filterModel, UUID resultsId) throws Exception {

        // Get Redis Cache, if it is there
        String datasetCache = getCache("dataset" + datasetId + branch);
        if(datasetCache != null) {
            System.out.println("cache found : serving from cache");
            return datasetCache;
        }
        System.out.println("no cache found");

         Dataset<Row> dfTotal;
            dfTotal = zoroService.getSparkDF(datasetId, branch, -1);


//        dfTotal.show();
//        System.out.println(dfTotal.schema());

//            Dataset<Row> df;
            Dataset<Row> df = dfTotal;

            if (filterModel.getColumns() != null) { // if columns null pass it as it is
                for (ColumnsModel columnsModel1 : filterModel.getColumns()) {
                    if (Objects.equals(columnsModel1.getType(), "string")) {  // TODO : other use cases
                        System.out.println("entering string");
                        dfTotal = dfTotal.filter(dfTotal.col(columnsModel1.getName()).like(columnsModel1.getValue()));
//                        dfTotal.show();
                    } else if (Objects.equals(columnsModel1.getType(), "integer") || Objects.equals(columnsModel1.getType(), "double")) {

                        if (Objects.equals(columnsModel1.getExpression(), "gt")) {
                            System.out.println("going in integer greater than");
                            dfTotal = dfTotal.filter(
                                    dfTotal.col(columnsModel1.getName()).gt(Integer.parseInt(columnsModel1.getValue()))
                            );

                        } else if (Objects.equals(columnsModel1.getExpression(), "lt")) {
                            dfTotal = dfTotal.filter(
                                    dfTotal.col(columnsModel1.getName()).lt(Integer.parseInt(columnsModel1.getValue()))
                            );

                        } else if (Objects.equals(columnsModel1.getExpression(), "eq")) {
                            dfTotal = dfTotal.filter(
                                    dfTotal.col(columnsModel1.getName()).equalTo(Integer.parseInt(columnsModel1.getValue()))
                            );
                        }

                    } else if (Objects.equals(columnsModel1.getType(), "boolean")) {
                        // TODO : Not tested
                        dfTotal = dfTotal.filter(dfTotal.col(columnsModel1.getName()).equalTo(columnsModel1.getValue()));
                    } else if (Objects.equals(columnsModel1.getType(), "date")) {
                        // TODO : Not tested
                        dfTotal = dfTotal.filter(dfTotal.col(columnsModel1.getName()).equalTo(columnsModel1.getValue()));
                    }

                }
            }


            if (filterModel.getConditions() != null) { // if columns null pass it as it is
                for (ConditionsModel conditionsModel : filterModel.getConditions()) {
                    if (Objects.equals(conditionsModel.getConditionType(), "join")) {
//                        System.out.println(" joining .... sksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllll");
                        Dataset<Row> dfJoin = zoroService.getSparkDF(conditionsModel.getDataset(), branch, 300); // TODO branch in future

                        dfTotal = dfTotal.join(dfJoin,
                                dfTotal.col(conditionsModel.getWhere().getSourceColumn())
                                        .equalTo(dfJoin.col(conditionsModel.getWhere().getDestinationColumn())),
                                "inner");

                    }
                    if (Objects.equals(conditionsModel.getConditionType(), "union")) {
//                        System.out.println(" unioning .... sksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllllsksdkdsksdksdlkdsllsdlsdldsldslsdllllllllllllll");
                        Dataset<Row> dfUnion = zoroService.getSparkDF(conditionsModel.getDataset(), branch, 300); // TODO branch in future

                        dfTotal = dfTotal.unionAll(dfUnion).distinct();

                    }
                }
            }

            // Example:
            /*
            dataframe1.join(dataframe2,
                dataframe2.col("id_device").equalTo(dataframe1.col("id_device")).
                        and(dataframe2.col("id_vehicule").equalTo(dataframe1.col("id_vehicule"))).
                        and(dataframe2.col("tracking_time").lt(dataframe1.col("tracking_time"))).
                        and(dataframe1.col("diffDate").lt(3888))
                )
                        .orderBy(dataframe2.col("tracking_time").desc())
             */

            if (filterModel.getSort() != null) {
                if (Objects.equals(filterModel.getSort().getDirection(), "desc")) {
                    dfTotal = dfTotal.sort(desc(filterModel.getSort().getColumn()));
                }
                if (Objects.equals(filterModel.getSort().getDirection(), "asc")) {
                    dfTotal = dfTotal.sort(asc(filterModel.getSort().getColumn()));
//                    dfTotal.show();
                }
            }

//        check if word is not null then set numerical to null
//        if word is null and numerical is not null set word to null
//        if less than and equalto are provided then get values
//        and if greater than provided then get values
//        and if equal to provided get values
//        check if rows exists
//        set limit for the rows with number
//        and area to get
//        if 0 get first 500 if 1 get last 1000 and if null get all.

            if (filterModel.getRows() != null) {
                if (filterModel.getRows().getAmount() == 0) {
                    dfTotal = dfTotal.limit(500);
                } else {
                    dfTotal = dfTotal.limit(filterModel.getRows().getAmount());
                }
            }

            df = dfTotal.limit(500);


//            return new ResponseEntity<>(df.toJSON().collectAsList().toString(), HttpStatus.OK);
//            return df.toJSON().collectAsList().toString();

        // Set Redis Cache
        setCache("dataset" + datasetId + branch, df.toJSON().collectAsList().toString(), null);

        return df.toJSON().collectAsList().toString();

//        HashMap<String, Object> result = new HashMap<>();
//
//        result.put("result", df.toJSON().collectAsList().toString());
//
//        SparkResults sparkResults = new SparkResults();
//        sparkResults.setId(resultsId);
//        sparkResults.setResults(result);
//
//        sparkResultsRepository.save(sparkResults);
//
//        // Sending to socket
//        SocketMessage textMessage = new SocketMessage();
//        textMessage.setMessage("success");
//
//        template.convertAndSend("/topic/sparkResults/" + sparkResults.getId(), textMessage);
    }
    public static void setSQLConfigDataCache(UUID chartUUID, String key) {
        if(chartUUID != null && lastResponseToSQLConfigDataRequest.get(chartUUID) != null) {
            setCache(key, lastResponseToSQLConfigDataRequest.get(chartUUID).toString(), null);
            System.out.println("setting cache on :: " + key);
        }
    }

}