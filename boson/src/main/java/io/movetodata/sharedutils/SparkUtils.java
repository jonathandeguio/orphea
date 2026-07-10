package io.movetodata.sharedutils;

import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.sharedutils.Exceptions.EnvConfigurationException;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataTypes;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.movetodata.sharedutils.Utils.isBase64;

@Configuration
public class SparkUtils {
    public static Map<String, String> getSparkConf() throws EnvConfigurationException {
        Map<String, String> sparkConf = new HashMap<>();

        sparkConf.put("spark.driver.extraJavaOptions=-Dlog4jspark.root.logger", "WARN,console");


        String backingFs = System.getenv("BACKING_FS");

        if (backingFs == null) {
            throw new EnvConfigurationException("Error: no backing FS defined");
        }

        switch (backingFs) {
            case "s3":
                sparkConf.put("spark.hadoop.fs.s3a.path.style.access", "true");
                sparkConf.put("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
                sparkConf.put("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console");
                sparkConf.put("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED");
                sparkConf.put("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED");
                sparkConf.put("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED");
                sparkConf.put("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED");
                sparkConf.put("spark.sql.legacy.timeParserPolicy", "LEGACY");
                sparkConf.put("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"));
                sparkConf.put("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"));
                sparkConf.put("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"));
                sparkConf.put("spark.jars.packages", "org.apache.hadoop:hadoop-aws:3.3.2");
                break;

            case "gs":
                sparkConf.put("spark.eventLog.enabled", "true");
                sparkConf.put("spark.eventLog.dir", "gs://" + System.getenv("GS_BUCKET") + "/movetodata/spark-streaming");
                sparkConf.put("spark.hadoop.google.cloud.auth.service.account.enable", "true");
                sparkConf.put("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/root/google_creds.json");
                break;

            case "hdfs":
                sparkConf.put("spark.eventLog.enabled", "true");
                sparkConf.put("spark.eventLog.dir", System.getenv("HDFS_ENDPOINT") + "/movetodata/spark-streaming");
                sparkConf.put("spark.hadoop.fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
                break;

            case "localfs":
                sparkConf.put("spark.eventLog.enabled", "true");
                sparkConf.put("spark.eventLog.dir", System.getenv("LOCAL_FS_DIRECTORY") + "/spark-streaming");
                break;

            default:
                throw new EnvConfigurationException("Error: unsupported backing FS");
        }

        return sparkConf;
    }

    private static SparkSession createS3Session(String applicationName, BuildType buildType) {
        if (buildType == BuildType.DEFAULT) {
            return SparkSession.builder()
                    .master(sparkMaster())
                    .appName(applicationName)
                    .config("spark.hadoop.fs.s3a.path.style.access", true)
                    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
                    .config("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"))
                    .config("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"))
                    .config("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"))
                    .getOrCreate();

        } else {
            return SparkSession.builder()
                    .appName(applicationName)
                    .master(sparkMaster())
                    .config("spark.hadoop.fs.s3a.path.style.access", true)
                    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
                    .config("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"))
                    .config("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"))
                    .config("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"))
                    .getOrCreate();
        }
    }

    private static SparkSession createGSSession(String applicationName, BuildType buildType) throws IOException {
        String googleCloudCredentials = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        if (isBase64(googleCloudCredentials)) {
            googleCloudCredentials = new String(Base64.decodeBase64(googleCloudCredentials));
        }
        File file = new File(System.getenv("MOVETODATA_MOUNT_PATH") + "/google_creds.json");
        try (FileWriter fileWriter = new FileWriter(file.getPath())) {
            fileWriter.write(googleCloudCredentials);
        }

        if (buildType == BuildType.DEFAULT) {
            return SparkSession.builder()
                    .appName(applicationName)
                    .config("google.cloud.auth.service.account.enable", "true")
                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")

                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")

                    .getOrCreate();
        } else {

            return SparkSession.builder()
                    .appName(applicationName)
                    .master(sparkMaster())
                    .config("google.cloud.auth.service.account.enable", "true")
                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")

                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")

                    .getOrCreate();
        }
    }

    private static SparkSession createHDFSSession(String applicationName, BuildType buildType) {
//        Configuration conf = new Configuration();
//        conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
//        String namespace = System.getenv("NAMESPACE"); // String | The custom resource's namespace

        if (buildType == BuildType.DEFAULT) {

            return SparkSession.builder()
                    .appName(applicationName)
                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")

                    .getOrCreate();

        } else {
            return SparkSession.builder()
                    .appName(applicationName)
                    .master(sparkMaster())
                    .config("spark.deploy.mode", "cluster")
                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")

                    .getOrCreate();
        }

    }

    private static SparkSession createLocalFSSession(String applicationName, BuildType buildType) {

        if (buildType == BuildType.DEFAULT) {

//            String warehouseLocation = System.getenv("LOCAL_FS_DIRECTORY") + "/SQLTransforms/MoveToDataTransformTemporary/tmp";

            return SparkSession.builder()
                    .appName(applicationName)
                    .config("spark.driver.memory", "4g") // Testing if makes difference on windows
                    .config("spark.executor.memory", "4g") // Testing if makes difference on windows
                    .config("spark.driver.cores", "4")
                    .config("spark.executor.cores", "4")
                    .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                    .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                    .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                    .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
//                    .config("spark.eventLog.enabled", "true")
//                    .config("spark.eventLog.dir", "/tmp/sparkLogs")
                    .getOrCreate();


        }
        return SparkSession.builder()
                .appName(applicationName)
                .master(sparkMaster())
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
                .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
                .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
                .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
                .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
//                .config("spark.eventLog.enabled", "true")
//                .config("spark.eventLog.dir", "/tmp/sparkLogs")
                .getOrCreate();
    }

    private static String sparkMaster() {
//        ApiClient client = buildService.kubernetesClient();
//        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
//
//        CoreV1Api coreApi = new CoreV1Api(client);
//
//        String namespace = "default"; // replace with the namespace of the pod
//        String podName = "my-pod"; // replace with the name of the pod
//        V1Pod pod = coreApi.readNamespacedPod("podName", "movetodata", null);

//        return  "k8s://https://" + pod.getStatus().getHostIP() + ":443"; // or kubernetes.default.svc : https://kubernetes.default.svc:443
//        return  "k8s://https://" + System.getenv("KUBERNETES_SERVICE_HOST")  + ":" + System.getenv("KUBERNETES_SERVICE_PORT"); // or kubernetes.default.svc : https://kubernetes.default.svc:443

        String sparkMasterHost = "local[*]";

        if (System.getenv("SPARK_MASTER") != null) {
            sparkMasterHost = System.getenv("SPARK_MASTER");
        }

        return sparkMasterHost;
    }

    @Bean
    public static SparkSession sparkSessionForBuild() throws EnvConfigurationException, IOException {
        return createSparkSession(BuildType.PREVIEW);
    }

    public static SparkSession createSparkSession(BuildType buildType) throws EnvConfigurationException, IOException {
        String applicationName = UUID.randomUUID().toString();
        SparkSession spark = null;
        String backingFS = System.getenv("BACKING_FS");
        switch (backingFS) {
            case "s3":
                spark = createS3Session(applicationName, buildType);
                break;
            case "gs":
                spark = createGSSession(applicationName, buildType);
                break;
            case "hdfs":
                spark = createHDFSSession(applicationName, buildType);
                break;

            case "localfs":
                spark = createLocalFSSession(applicationName, buildType);
                break;
            default:
                throw new EnvConfigurationException("Error: no backing FS defined");
        }

        UDF2<String, String, Timestamp> myDate = (dateString, format) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);

            try {
                return new java.sql.Timestamp(sdf.parse(dateString).getTime());
            } catch (Exception e) {
                return null;
            }
        };

        spark.udf().register("stringToTimestamp", myDate, DataTypes.TimestampType);

        return spark;
    }

    @Bean
    public SparkSession sparkSession() throws EnvConfigurationException, IOException {
        return createSparkSession(BuildType.PREVIEW);
    }
}
