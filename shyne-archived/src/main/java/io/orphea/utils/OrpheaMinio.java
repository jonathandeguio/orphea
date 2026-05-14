package io.orphea.utils;

import com.amazonaws.services.kinesisvideo.model.InvalidEndpointException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
//@AllArgsConstructor
public class OrpheaMinio {

    public static MinioClient MinioConnection(String endpoint, String accessKey, String secretKey)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        // Create a minioClient with the MinIO server, its access key and secret key.
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public static StatObjectResponse MinioMetaData(String bucket, String objectName)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {

        MinioClient minioClient = MinioConnection(
                System.getenv("MINIO_ENDPOINT"),
                System.getenv("MINIO_ACCESS_KEY"),
                System.getenv("MINIO_SECRET_KEY"));


        StatObjectResponse stat =
                minioClient.statObject(
                        StatObjectArgs.builder().bucket(bucket).object(objectName).build());

        return stat;
    }

    public static Iterable<Result<Item>> MinioListObjects(String bucket)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        System.out.println("hellllllllloooooo.... ");

        return MinioClient.builder()
                .endpoint("http://192.168.1.97:9000")
                .credentials("miniominio", "miniominio")
                .build().listObjects(ListObjectsArgs.builder().bucket(bucket).build());

//        System.out.println("hellllllllloooooo.... ");
//
//        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).build());
    }

    public static List<MinioItem> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) throws InvalidEndpointException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        List objectList = new ArrayList();
        System.out.println("hellooo.... ");
        Iterable<Result<Item>> objectsIterator = MinioClient.builder()
                .endpoint("https://play.minio.io")
                .credentials("miniominio", "miniominio")
                .build()
                .listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(true)
                        .build());

        System.out.println("hellooo.... ");
        objectsIterator.forEach(i -> {
            try {
                objectList.add(new MinioItem(i.get()));
            } catch (Exception e) {
                new Exception(e);
            }
        });
        return objectList;
    }

    public String MinioUploader(String bucket, String sourceFile, String targetFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {

            MinioClient minioClient = MinioConnection(
                    System.getenv("MINIO_ENDPOINT"),
                    System.getenv("MINIO_ACCESS_KEY"),
                    System.getenv("MINIO_SECRET_KEY"));

            // Upload sourceFile as object name targetFile to bucket
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetFile)
                            .filename(sourceFile)
                            .build());

            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetFile)
                            .filename(sourceFile)
                            .build());
            System.out.println(
                    "'sourceFile' is successfully uploaded as "
                            + "object 'uploadedFile' to bucket 'dataset'.");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }

        return "File Uploaded successfully";
    }


//    public void MinioUploader(String absolutePath, String s) {
//    }
}
