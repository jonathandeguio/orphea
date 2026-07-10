package io.movetodata.sharedutils;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.BobEnums.FunnelStatus;
import io.movetodata.build.library.services.BuildLogService;
import io.movetodata.build.library.services.BuildService;
import io.movetodata.dataset.library.DTOs.CsvPreprocessingDTO;
import io.movetodata.dataset.library.repository.SchemaRepository;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.services.BranchService;
import io.movetodata.kitab.library.services.DatasetWritingTransactionService;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.sharedutils.Exceptions.EnvConfigurationException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.movetodata.sharedutils.BackFsFileUtils.getResourcePath;
import static io.movetodata.sharedutils.storage.FSMinio.MinioConnection;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

@Slf4j
@RequiredArgsConstructor
@Component
public class BackingFS {
    private final DatasetMappingService datasetMappingService;
    private final DatasetRepository datasetRepository;
    private final BranchRepository branchRepository;
    private final SchemaRepository schemaRepository;
    private final BranchService branchService;
    private final BuildLogService buildLogService;
    private final BackFsFileUtils backFsFileUtils;
    private final DatasetWritingTransactionService datasetWritingTransactionService;
    private final ResourceService resourceService;
    private final SparkDataService sparkDataService;
    private final BuildService buildService;

    public static List<Map<String, Object>> getListOfFiles(UUID datasetId, String branch, UUID transactionId) throws Exception {
        String datasetPath = String.format("dataset/%s/%s", datasetId, transactionId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        List<Map<String, Object>> rows = new ArrayList<>();

        String backingFS = System.getenv("BACKING_FS");

        if (Objects.isNull(backingFS)) {
            throw new Exception("Error: no backing FS defined");
        }

        switch (backingFS) {
            case "s3":
                String bucket = "movetodata";
                MinioClient minioClient = MinioConnection(System.getenv("MINIO_ENDPOINT"), System.getenv("MINIO_ACCESS_KEY"), System.getenv("MINIO_SECRET_KEY"));
                ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                        .bucket(bucket)
                        .startAfter(datasetPath)
                        .recursive(true)
                        .prefix(datasetPath)
                        .build();
                Iterable<Result<Item>> listFiles1 = minioClient.listObjects(listObjectsArgs);

                for (Result<Item> result : listFiles1) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("path", result.get().objectName().replace(datasetPath + "/", ""));
                    row.put("size", byteCountToDisplaySize(result.get().size()));
                    row.put("sizeInBytes", result.get().size());
                    row.put("lastModified", result.get().lastModified());
                    row.put("lastModifiedInUnixTimeStamp", result.get().lastModified()); // TODO : SDF format it
                    rows.add(row);
                }
                break;

            case "gs":
                Credentials credentials = CommonService.getGoogleCredentials();
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                String directory = String.format("movetodata/dataset/%s/%s/", datasetId, branch);
                Page<Blob> blobs = storage.list(System.getenv("GS_BUCKET"), Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(directory));

                for (Blob blob : blobs.iterateAll()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("path", blob.getName().replace(directory, ""));
                    row.put("size", byteCountToDisplaySize(blob.getSize()));
                    row.put("sizeInBytes", blob.getSize());
                    row.put("lastModifiedInUnixTimeStamp", blob.getUpdateTime());
                    row.put("lastModified", sdf.format(blob.getUpdateTime()));
                    rows.add(row);
                }
                break;

            case "hdfs":
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
                FileSystem fs = FileSystem.get(conf);
                Path path = new Path(getResourcePath("dataset", datasetId, String.valueOf(transactionId)));
                RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(path, false);

                while (listFiles.hasNext()) {
                    LocatedFileStatus locatedFileStatus = listFiles.next();
                    Map<String, Object> row = new HashMap<>();
                    row.put("path", locatedFileStatus.getPath().getName().replace(datasetPath + "/", ""));
                    row.put("size", byteCountToDisplaySize(locatedFileStatus.getLen()));
                    row.put("sizeInBytes", locatedFileStatus.getLen());
                    row.put("lastModifiedInUnixTimeStamp", locatedFileStatus.getModificationTime());
                    row.put("lastModified", sdf.format(locatedFileStatus.getModificationTime()));
                    rows.add(row);
                }
                break;

            case "localfs":
                String basePath = System.getenv("LOCAL_FS_DIRECTORY");

                java.nio.file.Path pathLocalFs = Paths.get(getResourcePath("dataset", datasetId, String.valueOf(transactionId)));
                File[] files = pathLocalFs.toFile().listFiles();

                for (File file : files) {
                    if (!file.isDirectory()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("path", file.getName());
                        row.put("size", byteCountToDisplaySize(file.length()));
                        row.put("sizeInBytes", file.length());
                        row.put("lastModifiedInUnixTimeStamp", file.lastModified());
                        row.put("lastModified", sdf.format(file.lastModified()));
                        rows.add(row);
                    }
                }
                break;


            default:
                throw new Exception("Error: invalid backing FS defined");
        }
        return rows;

    }

    public static HashMap<String, String> envVarBasedOnBackingFS(HashMap<String, String> envVars) throws EnvConfigurationException {


        String backingFS = System.getenv("BACKING_FS");

        if (Objects.isNull(backingFS)) {
            throw new EnvConfigurationException("Error: no backing FS defined");
        }

        switch (backingFS) {
            case "s3":
                envVars.put("MINIO_ENDPOINT", System.getenv("MINIO_ENDPOINT"));
                envVars.put("MINIO_ACCESS_KEY", System.getenv("MINIO_ACCESS_KEY"));
                envVars.put("MINIO_SECRET_KEY", System.getenv("MINIO_SECRET_KEY"));
                break;
            case "gs":
                envVars.put("GS_BUCKET", System.getenv("GS_BUCKET"));
                envVars.put("GOOGLE_CLOUD_CREDENTIALS", System.getenv("GOOGLE_CLOUD_CREDENTIALS"));
                break;
            case "hdfs":
                envVars.put("HDFS_ENDPOINT", System.getenv("HDFS_ENDPOINT"));
                break;

            case "localfs":
                envVars.put("LOCAL_FS_DIRECTORY", System.getenv("LOCAL_FS_DIRECTORY"));
                break;
            default:
                throw new EnvConfigurationException("Error: invalid backing FS defined");
        }
        return envVars;
    }

    public static ResourceSubtype getFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (Objects.equals(contentType, "text/csv") || Objects.equals(contentType, "text/plain")) {
            return ResourceSubtype.CSV;
        } else if (Objects.equals(contentType, "application/vnd.ms-excel") ||
                Objects.equals(contentType, "application/x-excel") ||
                Objects.equals(contentType, "application/x-msexcel")) {
            return ResourceSubtype.XLS; // .xls files
        } else if (Objects.equals(contentType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                Objects.equals(contentType, "application/xlsx")) {
            return ResourceSubtype.XLSX; // .xlsx files
        } else if (Objects.equals(contentType, "application/vnd.apache.parquet") || (Objects.equals(contentType, "application/octet-stream"))) {
            return ResourceSubtype.PARQUET;
        } else if (Objects.equals(contentType, "application/json")) {
            return ResourceSubtype.JSON; // JSON files
        } else {
            return ResourceSubtype.NONE;
        }
    }

    public void UploadCSV(List<MultipartFile> files, UUID datasetId, String branch, String mode, ResourceSubtype datasetSubType, ResourceSubtype fileType, UUID userId, UUID buildId, UUID newTransactionId, BuildTrigger buildTrigger, String sheetName, CsvPreprocessingDTO csvPreprocessingDTO) throws Exception {
        try {
            // These are done are not in dataset creation because, upload csv can be done multiple times even
            // after creating a dataset, hence everytime we need new transaction
            // Dataset Mapping Tasks
            // Finishing Build

            // Uploading
            buildLogService.createBuildLogEntry("Uploading " + datasetId, BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);
            datasetWritingTransactionService.startTransaction(datasetId, branch, userId, buildId);
            try {
                // This is to upload a file into dataset
                for (MultipartFile file : files) {
                    backFsFileUtils.UploadFile(file, "dataset", datasetId, String.valueOf(newTransactionId), mode, true);

                    if (fileType == ResourceSubtype.XLSX || fileType == ResourceSubtype.XLS) {
                        String path = getResourcePath("dataset", datasetId, String.valueOf(newTransactionId));
                        try {
                            sparkDataService.convertExcelToParquet(path, fileType, Utils.normalizeName(file.getOriginalFilename()), sheetName);

                            fileType = ResourceSubtype.PARQUET;
                            buildLogService.createBuildLogEntry("Excel processing successful", BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);
                        } catch (Exception e) {
                            throw e;
                        }

                    }
                }
            } finally {
                datasetWritingTransactionService.endTransaction(datasetId, branch, userId);
            }


            // Update kitab dataset also
            Optional<DatasetModel> optionalDatasetModel = datasetRepository.findById(datasetId);
            if (optionalDatasetModel.isEmpty()) {
                throw new Exception("Error: dataset not found");
            }
            DatasetModel datasetModel = optionalDatasetModel.get();
            Optional<BranchModel> branchModel = branchService.findBranchModelByDatasetIdAndBranch(datasetId, branch);

            if (branchModel.isEmpty()) {
                BranchModel branchModel1 = new BranchModel();
                branchModel1.setId(datasetId + branch);
                branchModel1.setDatasetId(datasetId);
                branchModel1.setBranch(branch);
                branchModel1.setType(fileType);
                branchModel1.setCreatedBy(userId);
                branchModel1.setCreatedAt(new Date());
//                    branchModel1.setEncoding(fileCharset.name());
                branchRepository.save(branchModel1);

                Set<BranchModel> newBranches = datasetModel.getBranches();
                newBranches.add(branchModel1);
                datasetModel.setBranches(newBranches);
            } else {
                branchModel.get().setType(fileType);
                branchModel.get().setUpdatedAt(new Date());
                branchModel.get().setUpdatedBy(userId);

                branchRepository.save(branchModel.get());
            }

            datasetRepository.save(datasetModel);
            if (csvPreprocessingDTO != null && fileType == ResourceSubtype.CSV) {
                sparkDataService.updateDatasetCustomSchemaForFunnel(datasetId, newTransactionId, branch, csvPreprocessingDTO);
            }

            // Finishing Build
            buildService.postTransform(datasetId, new ArrayList<>(), newTransactionId, branch, null, null, buildId, BuildTrigger.UPLOAD, userId);
            buildLogService.createBuildLogEntry("Processing Finished", BuildStage.FINISHED, FunnelStatus.INFO, buildId, newTransactionId);
        } catch (Error err) {
            log.error("Error : ", err);

            // Finishing Build
            buildLogService.checkpoint(buildId, datasetId, BuildStatus.FAILED, newTransactionId);
            buildLogService.createBuildLogEntryWithDebug("Build failed", err.getMessage(), BuildStage.FINISHED, FunnelStatus.FAILED, buildId, newTransactionId);
        }
    }

    @Transactional
    public void downloadFile(String downloadUrl, UUID datasetId, String branch, String mode, ResourceSubtype datasetSubType, ResourceSubtype fileType, UUID userId, UUID buildId, UUID newTransactionId, BuildTrigger buildTrigger, String sheetName) throws Exception {
        String fileName = "downloaded" + sheetName;
        try {
            buildLogService.createBuildLogEntry("Downloading " + datasetId, BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);
            datasetWritingTransactionService.startTransaction(datasetId, branch, userId, buildId);
            try {
                // This is to upload a file into dataset
                backFsFileUtils.downloadFile(downloadUrl, "dataset", datasetId, String.valueOf(newTransactionId), mode, Utils.normalizeName(fileName));

                if (fileType == ResourceSubtype.XLSX || fileType == ResourceSubtype.XLS) {
                    String path = getResourcePath("dataset", datasetId, String.valueOf(newTransactionId));
                    try {
                        sparkDataService.convertExcelToParquet(path, fileType, Utils.normalizeName(fileName), sheetName);

                        fileType = ResourceSubtype.PARQUET;
                        buildLogService.createBuildLogEntry("Excel processing successful", BuildStage.RUNNING, FunnelStatus.INFO, buildId, newTransactionId);
                    } catch (Exception e) {
                        buildLogService.createBuildLogEntry("Failed to convert to parquet", BuildStage.FINISHED, FunnelStatus.ERROR, buildId, newTransactionId);
                        throw e;
                    }

                }

            } catch (Exception ignored) {
            } finally {
                datasetWritingTransactionService.endTransaction(datasetId, branch, userId);
            }


            // Update kitab dataset also
            Optional<DatasetModel> optionalDatasetModel = datasetRepository.findById(datasetId);
            if (optionalDatasetModel.isEmpty()) {
                throw new Exception("Error: dataset not found");
            }
            DatasetModel datasetModel = optionalDatasetModel.get();
            Optional<BranchModel> branchModel = branchService.findBranchModelByDatasetIdAndBranch(datasetId, branch);

            if (branchModel.isEmpty()) {
                BranchModel branchModel1 = new BranchModel();
                branchModel1.setId(datasetId + branch);
                branchModel1.setDatasetId(datasetId);
                branchModel1.setBranch(branch);
                branchModel1.setType(fileType);
                branchModel1.setCreatedBy(userId);
                branchModel1.setCreatedAt(new Date());
//                    branchModel1.setEncoding(fileCharset.name());
                branchRepository.save(branchModel1);

                Set<BranchModel> newBranches = datasetModel.getBranches();
                newBranches.add(branchModel1);
                datasetModel.setBranches(newBranches);
            } else {
                branchModel.get().setType(fileType);
                branchModel.get().setUpdatedAt(new Date());
                branchModel.get().setUpdatedBy(userId);

                branchRepository.save(branchModel.get());
            }

            datasetRepository.save(datasetModel);
            resourceService.updateDatasetOnPostTransform(datasetId, branch, datasetSubType, userId, buildId, buildTrigger, newTransactionId);

            if (!schemaRepository.existsByDatasetIdAndBranchAndTransactionId(datasetId, branch, newTransactionId)) { // if no schema found then create it in db
                sparkDataService.createDBSchemaIfNotExists(datasetId, branch, newTransactionId, fileType, userId);
            }

            // Finishing Build
            buildLogService.checkpoint(buildId, datasetId, BuildStatus.SUCCESS, newTransactionId);
            buildLogService.createBuildLogEntry("Processing Finished", BuildStage.FINISHED, FunnelStatus.INFO, buildId, newTransactionId);
            datasetMappingService.postTransformDatasetMappingOperations(datasetId, branch, newTransactionId, buildId);

        } catch (Error err) {
            log.error("Error : ", err);

            // Finishing Build
            buildLogService.checkpoint(buildId, datasetId, BuildStatus.FAILED, newTransactionId);
            buildLogService.createBuildLogEntryWithDebug("Build failed", err.getMessage(), BuildStage.FINISHED, FunnelStatus.FAILED, buildId, newTransactionId);
        }
    }

}
