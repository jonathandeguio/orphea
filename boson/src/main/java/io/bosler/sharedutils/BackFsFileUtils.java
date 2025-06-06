package io.bosler.sharedutils;

import com.google.auth.Credentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.bosler.dataset.library.Keys.DatasetMappingKey;
import io.bosler.dataset.library.models.DatasetMappingModel;
import io.bosler.dataset.library.repository.DatasetMappingRepository;
import io.bosler.sharedutils.Exceptions.EnvConfigurationException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.bosler.sharedutils.storage.FSMinio.MinioConnection;

@Slf4j
@RequiredArgsConstructor
@Component
public class BackFsFileUtils {

    private final DatasetMappingRepository datasetMappingRepository;
    private final RestTemplate restTemplate;

    public static String getResourcePath(String type, UUID resourceId, String transactionId) throws EnvConfigurationException {
        String backingFs = System.getenv("BACKING_FS");
        if (backingFs == null || backingFs.isEmpty()) {
            throw new EnvConfigurationException("Error: no backing FS defined");
        }

        String datasetPath;

        switch (backingFs) {
            case "s3":
                datasetPath = String.format("s3a://bosler/%s/%s/%s", type, resourceId, transactionId);
                break;
            case "gs":
                datasetPath = String.format("gs://%s/bosler/%s/%s/%s", System.getenv("GS_BUCKET"), type, resourceId, transactionId);
                break;
            case "hdfs":
                datasetPath = String.format("%s/bosler/%s/%s/%s", System.getenv("HDFS_ENDPOINT"), type, resourceId, transactionId);
                break;
            case "localfs":
                datasetPath = String.format("%s/%s/%s/%s", System.getenv("LOCAL_FS_DIRECTORY"), type, resourceId, transactionId);
                break;
            default:
                throw new EnvConfigurationException("Unknown backing FS: " + backingFs);
        }

        return datasetPath;
    }

    public static MultipartFile convertFileToMultipartFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(),
                file.getName(), "text/plain", input);
        input.close();
        return multipartFile;
    }

    public static List<String> getExcelSheetNames(String excelFilePath) throws Exception {
        List<String> sheetNames = new ArrayList<>();
        String backingFs = System.getenv("BACKING_FS");
        switch (backingFs) {
            case "localfs":
                // Open the Excel file using Apache POI
                try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    // Get the number of sheets and loop through them to get names
                    int numberOfSheets = workbook.getNumberOfSheets();
                    for (int i = 0; i < numberOfSheets; i++) {
                        sheetNames.add(workbook.getSheetName(i));
                    }
                }
                break;
            default:
                throw new Exception("Not a valid backingfs to do this");
        }


        return sheetNames;
    }

    public void UploadFile(MultipartFile file, String type, UUID resourceId, String transactionIdOrBranch, String mode, boolean useOriginalFilename) throws Exception {

        String storedFileName = Utils.normalizeName(file.getOriginalFilename());
//        String storedFileName = file.getOriginalFilename();

        if (!useOriginalFilename) {
            if (Objects.equals(type, "file")) {
                storedFileName = String.valueOf(resourceId);
            }
        }

        String backingFs = System.getenv("BACKING_FS");

        switch (backingFs) {
            case "s3":
                InputStreamReader reader;
                ByteArrayInputStream inputStreamOriginal;
                inputStreamOriginal = new ByteArrayInputStream(file.getBytes());

                String bucket = "bosler";
                String targetFilePath = type + "/" + resourceId + "/" + transactionIdOrBranch + "/" + storedFileName;

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

                PutObjectArgs putObject = PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(targetFilePath)
                        .stream(inputStreamOriginal, -1, 10485760)
                        .build();

                minioClient.putObject(putObject);
                break;

            case "gs":
                Credentials credentials = CommonService.getGoogleCredentials();

                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "bosler/" + type + "/" + resourceId + "/" + transactionIdOrBranch + "/" + storedFileName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

                storage.create(blobInfo, file.getBytes());
                break;

            case "hdfs":
                // Set up the configuration
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));

                // Get a reference to the filesystem
                FileSystem fs = FileSystem.get(conf);

                Path path = new Path("/bosler/" + type + "/" + resourceId + "/" + transactionIdOrBranch + "/" + storedFileName);

                // Create an FSDataOutputStream to write to the file
                FSDataOutputStream outputStream = fs.create(path, true); // TODO : append mode also

                // Get the bytes object you want to write to the file
                byte[] bytes = file.getBytes();

                // Write the bytes to the file
                outputStream.write(bytes);

                // Close the output stream
                outputStream.close();
                break;

            case "localfs":
                File datasetDirectory = new File(getResourcePath(type, resourceId, transactionIdOrBranch) + "/");
                System.out.println(" DIRECTORY : " + datasetDirectory.getAbsolutePath());
                if (!datasetDirectory.exists()) {
                    System.out.println(" DIRECTORY CREATED ");
                    datasetDirectory.mkdirs();
                }

                File csvFile = new File(getResourcePath(type, resourceId, transactionIdOrBranch) + "/" + storedFileName);

                log.info("Writing to localFS: " + csvFile.getAbsolutePath());
                try {
                    file.transferTo(csvFile.getAbsoluteFile());
                } catch (IOException e) {
                    log.error("Failed to write to localFS: " + e.getMessage());
                }

                break;

            default:
                throw new Exception("Error: no backing FS defined");
        }

    }

    public void downloadFile(String downloadUrl, String type, UUID resourceId, String transactionIdOrBranch, String mode, String fileName) throws Exception {

        String storedFileName = fileName;
        String backingFs = System.getenv("BACKING_FS");

        switch (backingFs) {
            case "localfs":
                File datasetDirectory = new File(getResourcePath(type, resourceId, transactionIdOrBranch) + "/");
                log.info(" DIRECTORY : " + datasetDirectory.getAbsolutePath());
                if (!datasetDirectory.exists()) {
                    log.info("Directory created");
                    datasetDirectory.mkdirs();
                }

                File csvFile = new File(getResourcePath(type, resourceId, transactionIdOrBranch) + "/" + storedFileName);

                log.info("Writing to localFS: " + csvFile.getAbsolutePath());

                // Fetch the file resource from the URL
                Resource resource = restTemplate.getForObject(downloadUrl, Resource.class);

                // Open input and output streams to handle file download
                assert resource != null;
                try (InputStream inputStream = resource.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(csvFile)) {
                    StreamUtils.copy(inputStream, outputStream);
                }

                break;

            default:
                throw new Exception("Error: no backing FS defined for download file");
        }
    }

    public void updateFile(byte[] bytes, String type, UUID resourceId, String branch, String mode) throws Exception {
        String storedFileName = String.valueOf(resourceId);

        String backingFs = System.getenv("BACKING_FS");
        String transactionId = branch;
        if (Objects.equals(type, "dataset")) {
            // Getting transactionId
            DatasetMappingKey key = new DatasetMappingKey(resourceId, branch);
            DatasetMappingModel datasetMappingModel = datasetMappingRepository.getReferenceById(key);
            transactionId = String.valueOf(datasetMappingModel.getCurrentTransaction());
        }

        switch (backingFs) {
            case "s3":
                InputStreamReader reader;
                ByteArrayInputStream inputStreamOriginal;
                inputStreamOriginal = new ByteArrayInputStream(bytes);

                String bucket = "bosler";
                String targetFilePath = type + "/" + resourceId + "/" + transactionId + "/" + storedFileName;

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

                PutObjectArgs putObject = PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(targetFilePath)
                        .stream(inputStreamOriginal, -1, 10485760)
                        .build();

                minioClient.putObject(putObject);
                break;

            case "gs":
                Credentials credentials = CommonService.getGoogleCredentials();

                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "bosler/" + type + "/" + resourceId + "/" + transactionId + "/" + storedFileName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

                storage.create(blobInfo, bytes);
                break;

            case "hdfs":
                // Set up the configuration
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));

                // Get a reference to the filesystem
                FileSystem fs = FileSystem.get(conf);

                Path path = new Path("/bosler/" + type + "/" + resourceId + "/" + transactionId + "/" + storedFileName);

                // Create an FSDataOutputStream to write to the file
                FSDataOutputStream outputStream = fs.create(path, true); // TODO : append mode also

                // Get the bytes object you want to write to the file
                // Write the bytes to the file
                outputStream.write(bytes);

                // Close the output stream
                outputStream.close();
                break;

            case "localfs":
                File datasetDirectory = new File(getResourcePath(type, resourceId, transactionId) + "/");
                if (!datasetDirectory.exists()) {
                    datasetDirectory.mkdirs();
                }

                File csvFile = new File(getResourcePath(type, resourceId, transactionId) + "/" + storedFileName);

                System.out.println("Writing to localFS: " + csvFile.getAbsolutePath());
                FileWriter fileWriter = new FileWriter(csvFile.getPath());
                fileWriter.write(new String(bytes));
                fileWriter.close();
                break;

            default:
                throw new Exception("Error: no backing FS defined");
        }

    }

}
