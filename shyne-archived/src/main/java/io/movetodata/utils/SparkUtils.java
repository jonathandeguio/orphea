package io.movetodata.utils;

import com.amazonaws.thirdparty.apache.codec.binary.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ApiException;
import io.movetodata.BobEnums.BuildStage;
import io.movetodata.BobEnums.BuildStatus;
import io.movetodata.library.models.SchemaModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static io.movetodata.utils.Utils.isBase64;
import static org.apache.spark.sql.functions.*;

public class SparkUtils {


    public static SparkSession sparkSession() throws Exception {
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
            case "localfs":
                spark = createLocalFSSession(applicationName);
                break;
            default:
                throw new Exception("Error: no backing FS defined");
        }
        return spark;
    }

    private static SparkSession createS3Session(String applicationName) throws IOException, ApiException {
        String warehouseLocation = "/movetodata/dataset/SQLTransformation/tmp";
        return SparkSession.builder()
//                .master("local[*]")
                .appName(applicationName)
                .config("spark.hadoop.fs.s3a.endpoint", System.getenv("MINIO_ENDPOINT"))
                .config("spark.hadoop.fs.s3a.access.key", System.getenv("MINIO_ACCESS_KEY"))
                .config("spark.hadoop.fs.s3a.secret.key", System.getenv("MINIO_SECRET_KEY"))
                .config("spark.hadoop.fs.s3a.path.style.access", true)
                .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")

                .config("spark.sql.warehouse.dir", warehouseLocation)

                .enableHiveSupport()

                .getOrCreate();
    }

    private static SparkSession createGSSession(String applicationName) throws IOException, ApiException {
        String warehouseLocation = "/movetodata/dataset/SQLTransformation/tmp";
        String googleCloudCredentials = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        if (isBase64(googleCloudCredentials)) {
            googleCloudCredentials = new String(Base64.decodeBase64(googleCloudCredentials));
        }
        File file = new File(System.getenv("MOVETODATA_MOUNT_PATH") + "/google_creds.json");
        try (FileWriter fileWriter = new FileWriter(file.getPath())) {
            fileWriter.write(googleCloudCredentials);
        }
        return SparkSession.builder()
                .appName(applicationName)
                .config("google.cloud.auth.service.account.enable", "true")
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .config("google.cloud.auth.service.account.json.keyfile", System.getenv("MOVETODATA_MOUNT_PATH") + "/google_creds.json")

                .config("spark.sql.warehouse.dir", warehouseLocation)
                .enableHiveSupport()

                .getOrCreate();
    }

    private static SparkSession createHDFSSession(String applicationName) throws IOException, ApiException {
        String warehouseLocation = "/movetodata/dataset/SQLTransformation/tmp";
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
        return SparkSession.builder()
                .appName(applicationName)
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .config("spark.hadoop.fs.defaultFS", System.getenv("HDFS_ENDPOINT"))

                .config("spark.sql.warehouse.dir", warehouseLocation)
                .enableHiveSupport()

                .getOrCreate();

    }

    private static SparkSession createLocalFSSession(String applicationName) throws IOException, ApiException {
        String warehouseLocation = System.getenv("LOCAL_FS_DIRECTORY") + "/SQLTransforms/MoveToDataTransformTemporary/tmp";
        return SparkSession.builder()
                .appName(applicationName)
                .config("spark.driver.extraJavaOptions", "-Dlog4jspark.root.logger=WARN,console")
                .config("spark.eventLog.enabled", "true")
                .config("spark.eventLog.dir", System.getenv("LOCAL_FS_DIRECTORY") + "/spark-streaming")

                .config("spark.sql.warehouse.dir", warehouseLocation)
                .enableHiveSupport()

                .getOrCreate();

    }

    public static String columnDataType(Dataset<Row> dataset, String colName) {
        StructType schema = dataset.schema();
        return schema.apply(colName).dataType().typeName();
    }

    public static Dataset<Row> getSparkDF(UUID datasetId, String branch, int limit) throws Exception {

        SparkSession spark = SparkUtils.sparkSession();
        Dataset<Row> dfTotal = null;

        String transactionId = "00000000-0000-0000-0000-000000000000"; // This is to get initial transaction for source

        ObjectMapper objectMapper = new ObjectMapper();

        String branchType = objectMapper.readTree(BosonApiCalls.getBranchType(String.valueOf(datasetId), branch).get("response")).get("branchType").asText();

        String datasetPath = objectMapper.readTree(BosonApiCalls.getPhysicalPath(datasetId, branch, transactionId).get("response")).get("physicalPath").asText();

        if(branchType == null){
            BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Branch type is not valid", null);
        }

        if (branchType.equals("RAW")) {

            String encoding = objectMapper.readTree(BosonApiCalls.getEncoding(datasetId, branch).get("response")).get("encoding").asText();

            String response = ApiCaller.callApi("/api/dataset/schema/" + datasetId + "/" + branch + "/" + transactionId, "GET", System.getenv("ACCESS_TOKEN"), null, null).get("response");

            SchemaModel schemaModelActive = objectMapper.readValue(response, SchemaModel.class);

            Dataset<Row> reader = spark.read()
                    .format("csv")
                    .option("sep", schemaModelActive.getCustomSchema().getFieldDelimiter())
                    .option("delimiter", schemaModelActive.getCustomSchema().getFieldDelimiter())
                    .option("quote", schemaModelActive.getCustomSchema().getEscapeCharacter())
                    .option("escape", schemaModelActive.getCustomSchema().getEscapeCharacter())
                    .option("timestampFormat", schemaModelActive.getCustomSchema().getDateFormat().get("timestampDefault"))
                    .option("dateFormat", schemaModelActive.getCustomSchema().getDateFormat().get("dateDefault"))
                    .option("ignoreLeadingWhiteSpace", true)
                    .schema(schemaModelActive.getSchema())
                    .option("header", "true")
                    .option("encoding", encoding)
                    .load(datasetPath);

            if (limit > -1) {
                dfTotal = reader.limit(limit);
            } else {
                dfTotal = reader;
            }

            String preDefinedFormat = "";
            Map<String, String> dateFormatMap = schemaModelActive.getCustomSchema().getDateFormat();

            for (StructField element : schemaModelActive.getSchema().fields()) {
                if (dateFormatMap.containsKey(element.name())) {
                    preDefinedFormat = dateFormatMap.get(element.name());
                }

                if (element.dataType().typeName().equals("timestamp")) {
                    preDefinedFormat = dateFormatMap.getOrDefault("timestampDefault", preDefinedFormat);
                    dfTotal = dfTotal.withColumn(element.name(), to_timestamp(col(element.name()), preDefinedFormat));
                } else if (element.dataType().typeName().equalsIgnoreCase("date")) {
                    preDefinedFormat = dateFormatMap.getOrDefault("dateDefault", preDefinedFormat);
                    dfTotal = dfTotal.withColumn(element.name(), to_date(col(element.name()), preDefinedFormat));
                }
            }
        } else if (branchType.equals("PARQUET")) {
            Dataset<Row> reader = spark.read()
                    .format("parquet")
                    .option("header", "true")
                    .parquet(datasetPath);

            if (limit > -1) {
                dfTotal = reader.limit(limit);
            } else {
                dfTotal = reader;
            }
        } else {
            BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Branch type is not valid", null);
        }

        return dfTotal;
    }
}
