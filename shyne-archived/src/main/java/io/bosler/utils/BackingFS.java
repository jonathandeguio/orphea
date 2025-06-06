package io.bosler.utils;

import com.amazonaws.util.StringInputStream;
import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static io.bosler.utils.BoslerMinio.MinioConnection;
import static io.bosler.utils.Utils.isBase64;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

public class BackingFS {

    public static boolean deleteFilesFromGS(UUID datasetId, String branch) {
        try {
            Credentials credentials = getGoogleCredentials();
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            String prefix = "bosler/dataset/" + datasetId + "/" + branch + "/";
            Page<Blob> files = storage.list(System.getenv("GS_BUCKET"), Storage.BlobListOption.prefix(prefix));
            for (Blob blob : files.iterateAll()) {
                blob.delete();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deleteFilesFromHDFS(UUID datasetId, String branch) {
        String hdfsEndpoint = System.getenv("HDFS_ENDPOINT");
        if (hdfsEndpoint == null || hdfsEndpoint.isEmpty()) {
            return false;
        }

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsEndpoint);

        try (FileSystem fs = FileSystem.get(conf)) {
            Path path = new Path(String.format("/bosler/dataset/%s/%s/", datasetId, branch));
            boolean isDeleted = fs.delete(path, true);
            return isDeleted;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFilesFromLOCALFS(UUID datasetId, String branch) throws Exception {

        java.nio.file.Path pathLocalFs = Paths.get(getDatasetPathWithBranch(datasetId, branch));

        File[] files = pathLocalFs.toFile().listFiles();

        try {
            for (File file : files) {
                boolean isDeleted = file.delete();
                return isDeleted;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean deleteFilesFromS3(UUID datasetId, String branch) {
        String minioEndpoint = System.getenv("MINIO_ENDPOINT");
        String minioAccessKey = System.getenv("MINIO_ACCESS_KEY");
        String minioSecretKey = System.getenv("MINIO_SECRET_KEY");
        if (minioEndpoint == null || minioEndpoint.isEmpty()
                || minioAccessKey == null || minioAccessKey.isEmpty()
                || minioSecretKey == null || minioSecretKey.isEmpty()) {
            return false;
        }

        String bucket = "dataset";
        String targetFilePath = datasetId.toString() + "/" + branch;

        try {
            MinioClient minioClient = MinioConnection(minioEndpoint, minioAccessKey, minioSecretKey);

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


    public static void deleteDatasetFiles(UUID datasetId, String branch) throws Exception {
        String backingFs = System.getenv("BACKING_FS");
        if (backingFs == null || backingFs.isEmpty()) {
            throw new Exception("Error: no backing FS defined");
        }

        switch (backingFs) {
            case "s3":
                BackingFS.deleteFilesFromS3(datasetId, branch);
                break;
            case "gs":
                BackingFS.deleteFilesFromGS(datasetId, branch);
                break;
            case "hdfs":
                BackingFS.deleteFilesFromHDFS(datasetId, branch);
                break;
            case "localfs":
                BackingFS.deleteFilesFromLOCALFS(datasetId, branch);
                break;
            default:
                throw new Exception("Unknown backing FS: " + backingFs);
        }
    }

    public static Credentials getGoogleCredentials() throws IOException {

        String google_cloud_credentials_decoded;

        if (isBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS"))) {
            google_cloud_credentials_decoded = new String(Base64.getDecoder().decode(System.getenv("GOOGLE_CLOUD_CREDENTIALS")));
        } else {
            google_cloud_credentials_decoded = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        }

        InputStream google_cloud_credentials = new StringInputStream(google_cloud_credentials_decoded);

        return GoogleCredentials.fromStream(google_cloud_credentials);
    }

    public static String getDatasetPathWithBranch(UUID datasetId, String transcationId) throws Exception {
        return getDatasetPath(datasetId) + "/" + transcationId;
    }

    public static String getDatasetPath(UUID datasetId) throws Exception {
        String backingFs = System.getenv("BACKING_FS");
        if (backingFs == null || backingFs.isEmpty()) {
            throw new Exception("Error: no backing FS defined");
        }

        String datasetPath;

        switch (backingFs) {
            case "s3":
                datasetPath = String.format("s3a://bosler/dataset/%s", datasetId);
                break;
            case "gs":
                datasetPath = String.format("gs://%s/bosler/dataset/%s", System.getenv("GS_BUCKET"), datasetId);
                break;
            case "hdfs":
                datasetPath = String.format("%s/bosler/dataset/%s", System.getenv("HDFS_ENDPOINT"), datasetId);
                break;
            case "localfs":
                datasetPath = String.format("%s/dataset/%s", System.getenv("LOCAL_FS_DIRECTORY"), datasetId);
                break;
            default:
                throw new Exception("Unknown backing FS: " + backingFs);
        }

        return datasetPath;
    }

}
