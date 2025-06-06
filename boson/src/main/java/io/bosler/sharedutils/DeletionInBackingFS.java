package io.bosler.sharedutils;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.bosler.dataset.library.models.DatasetMappingTransactionModel;
import io.bosler.dataset.library.repository.DatasetMappingTransactionRepository;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static io.bosler.sharedutils.BackFsFileUtils.getResourcePath;
import static io.bosler.sharedutils.storage.FSMinio.MinioConnection;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeletionInBackingFS {

    private final DatasetMappingTransactionRepository datasetMappingTransactionRepository;

    public static boolean deleteFilesFromGS(String type, UUID datasetId, UUID transactionId) {
        try {
            Credentials credentials = CommonService.getGoogleCredentials();
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            String prefix = "bosler/" + type + "s/" + datasetId + "/" + transactionId + "/";
            Page<Blob> files = storage.list(System.getenv("GS_BUCKET"), Storage.BlobListOption.prefix(prefix));
            for (Blob blob : files.iterateAll()) {
                blob.delete();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deleteFilesFromHDFS(String type, UUID datasetId, UUID transactionId) throws Exception {
        String hdfsEndpoint = System.getenv("HDFS_ENDPOINT");
        if (hdfsEndpoint == null || hdfsEndpoint.isEmpty()) {
            return false;
        }

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsEndpoint);

        try (FileSystem fs = FileSystem.get(conf)) {
            Path path = new Path(String.format("/bosler/" + type + "/%s/%s/", datasetId, transactionId));
            boolean isDeleted = fs.delete(path, true);
            return isDeleted;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFilesFromLOCALFS(String type, UUID datasetId, UUID transactionId) throws Exception {

        java.nio.file.Path pathLocalFs = Paths.get(getResourcePath(type, datasetId, String.valueOf(transactionId)));
        File[] files = pathLocalFs.toFile().listFiles();

        try {
            for (File file : files) {
                log.info(">>>> D NAME : " + file.getName());
                log.info(">>>> D PATH : " + file.getPath());
                boolean isDeleted = file.delete();
                return isDeleted;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean deleteFilesFromS3(String type, UUID datasetId, UUID transactionId) throws Exception {
        String minioEndpoint = System.getenv("MINIO_ENDPOINT");
        String minioAccessKey = System.getenv("MINIO_ACCESS_KEY");
        String minioSecretKey = System.getenv("MINIO_SECRET_KEY");
        if (minioEndpoint == null || minioEndpoint.isEmpty()
                || minioAccessKey == null || minioAccessKey.isEmpty()
                || minioSecretKey == null || minioSecretKey.isEmpty()) {
            return false;
        }

        String bucket = "bosler";
        String targetFilePath = String.format("%ss/%s/%s/", type, datasetId, transactionId);

        try {
            MinioClient minioClient = MinioConnection(minioEndpoint, minioAccessKey, minioSecretKey);

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucket).startAfter(targetFilePath).recursive(true).build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                // Delete the object
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
            }

            RemoveObjectArgs removeObject = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(targetFilePath)
                    .build();

            minioClient.removeObject(removeObject);

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void deleteDatasetFiles(String type, UUID datasetId, String branch) throws Exception {
        List<DatasetMappingTransactionModel> transactions = datasetMappingTransactionRepository.findByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
        for (DatasetMappingTransactionModel transaction : transactions) {
            deleteDatasetFilesWrapperBasedOnStorageType(type, datasetId, transaction.getId());
        }

    }

    public void deleteDatasetFilesWrapperBasedOnStorageType(String type, UUID datasetId, UUID transaction) throws Exception {
        String backingFs = System.getenv("BACKING_FS");
        if (backingFs == null || backingFs.isEmpty()) {
            throw new Exception("Error: no backing FS defined");
        }

        switch (backingFs) {
            case "s3":
                deleteFilesFromS3(type, datasetId, transaction);
                break;
            case "gs":
                deleteFilesFromGS(type, datasetId, transaction);
                break;
            case "hdfs":
                deleteFilesFromHDFS(type, datasetId, transaction);
                break;
            case "localfs":
                deleteFilesFromLOCALFS(type, datasetId, transaction);
                break;
            default:
                throw new Exception("Unknown backing FS: " + backingFs);
        }
    }
}
