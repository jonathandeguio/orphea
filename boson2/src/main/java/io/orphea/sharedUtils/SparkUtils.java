package io.orphea.sharedUtils;

import java.util.HashMap;
import java.util.Map;


public class SparkUtils {
    public static Map<String, String>  getSparkConf() throws Exception {
        Map<String, String> sparkConf = new HashMap<>();

        sparkConf.put("spark.driver.extraJavaOptions=-Dlog4jspark.root.logger", "WARN,console");


        String backingFs = System.getenv("BACKING_FS");

        if (backingFs == null) {
            throw new Exception("Error: no backing FS defined");
        }

        switch (backingFs) {
            case "s3":
                sparkConf.put("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"));
                sparkConf.put("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"));
                sparkConf.put("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"));
                sparkConf.put("spark.hadoop.fs.s3a.path.style.access", "true");
                sparkConf.put("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
                sparkConf.put("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
                sparkConf.put("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
                sparkConf.put("spark.hadoop.fs.s3a.multiobjectdelete.enable", "false");
                sparkConf.put("spark.hadoop.fs.s3a.fast.upload", "true");

                sparkConf.put("spark.eventLog.enabled", "false"); // TODO : fix and enable eventLog, this is used by spark history server
                sparkConf.put("spark.eventLog.dir", "s3a://spark-streaming/checkpoint/");
                break;

            case "gs":
                sparkConf.put("spark.eventLog.enabled", "true");
                sparkConf.put("spark.eventLog.dir", "gs://" + System.getenv("GS_BUCKET") + "/orphea/spark-streaming");
                sparkConf.put("spark.hadoop.google.cloud.auth.service.account.enable", "true");
                sparkConf.put("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/root/google_creds.json");
                break;

            case "hdfs":
                sparkConf.put("spark.eventLog.enabled", "true");
                sparkConf.put("spark.eventLog.dir", System.getenv("HDFS_ENDPOINT") + "/orphea/spark-streaming");
                sparkConf.put("spark.hadoop.fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
                break;

            default:
                throw new Exception("Error: unsupported backing FS");
        }

        return sparkConf;
    }
}
