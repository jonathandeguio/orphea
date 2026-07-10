package io.movetodata.sharedUtils;

import com.amazonaws.util.StringInputStream;
import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.tomcat.util.codec.binary.Base64;

import static io.movetodata.sharedUtils.Utils.isBase64;
import static io.movetodata.sharedUtils.storage.MoveToDataMinio.MinioConnection;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackingFS {

    public static boolean deleteFilesFromGS(UUID datasetId, String branch) {
        try {
            Credentials credentials = getGoogleCredentials();
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            String prefix = "movetodata/datasets/" + datasetId + "/" + branch + "/";
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
            Path path = new Path(String.format("/movetodata/datasets/%s/%s/", datasetId, branch));
            boolean isDeleted = fs.delete(path, true);
            return isDeleted;
        } catch (IOException e) {
            return false;
        }
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

        String bucket = "movetodata";
        String targetFilePath = String.format("datasets/%s/%s/", datasetId, branch);

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
            default:
                throw new Exception("Unknown backing FS: " + backingFs);
        }
    }

    public static Credentials getGoogleCredentials() throws IOException {

        String google_cloud_credentials_decoded;

        if (isBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS"))) {
            google_cloud_credentials_decoded = new String(Base64.decodeBase64(System.getenv("GOOGLE_CLOUD_CREDENTIALS")));
        } else {
            google_cloud_credentials_decoded = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        }

        InputStream google_cloud_credentials = new StringInputStream(google_cloud_credentials_decoded);

        return GoogleCredentials.fromStream(google_cloud_credentials);
    }

    public static String getDatasetPath(UUID datasetId, String branch) throws Exception {
        String backingFs = System.getenv("BACKING_FS");
        if (backingFs == null || backingFs.isEmpty()) {
            throw new Exception("Error: no backing FS defined");
        }

        String datasetPath;

        switch (backingFs) {
            case "s3":
                datasetPath = String.format("s3a://movetodata/datasets/%s/%s", datasetId, branch);
                break;
            case "gs":
                datasetPath = String.format("gs://%s/movetodata/datasets/%s/%s", System.getenv("GS_BUCKET"), datasetId, branch);
                break;
            case "hdfs":
                datasetPath = String.format("%s/movetodata/datasets/%s/%s", System.getenv("HDFS_ENDPOINT"), datasetId, branch);
                break;
            default:
                throw new Exception("Unknown backing FS: " + backingFs);
        }

        return datasetPath;
    }


    public static List<Map<String, Object>> getListOfFiles(UUID datasetId, String branch) throws Exception {
        String datasetPath = String.format("datasets/%s/%s", datasetId, branch);
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
                Credentials credentials = BackingFS.getGoogleCredentials();
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                String directory = String.format("movetodata/datasets/%s/%s/", datasetId, branch);
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
                Path path = new Path(String.format("/movetodata/datasets/%s/%s/", datasetId, branch));
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

            default:
                throw new Exception("Error: invalid backing FS defined");
        }
        return rows;

    }

    public static HashMap<String, String> envVarBasedOnBackingFS(HashMap<String, String > envVars) throws Exception {


        String backingFS = System.getenv("BACKING_FS");

        if (Objects.isNull(backingFS)) {
            throw new Exception("Error: no backing FS defined");
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
            default:
                throw new Exception("Error: invalid backing FS defined");
        }
        return envVars;
    }

}
