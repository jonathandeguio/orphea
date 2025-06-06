package io.bosler.build.shyne;

import com.fasterxml.jackson.databind.JsonNode;
import io.bosler.build.BobEnums.BuildType;
import io.bosler.build.shyne.Utils.BosonApiCalls;
import io.bosler.build.shyne.Utils.ShyneLogging;
import io.bosler.build.shyne.Utils.ShyneSparkUtils;
import io.bosler.build.shyne.Utils.Utils;
import io.bosler.dataset.library.models.ColumnStatsModel;
import io.bosler.dataset.library.models.SchemaModel;
import io.bosler.dataset.library.models.SparkResults;
import io.bosler.dataset.library.services.SparkService;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.sharedutils.SparkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.bosler.build.shyne.Utils.Utils.convertJsonNodeToClass;

@SpringBootApplication
@Slf4j
public class ColumnStats {
    static ShyneLogging logger = new ShyneLogging();

    public static void main(String[] args) throws Exception {

        // Below is for debug
//        try {
//            System.out.println("Waiting 3 mins for debug");
//            // Sleep for 3 minutes (5 * 60 * 1000 milliseconds)
//            Thread.sleep(3 * 60 * 1000);
//        } catch (InterruptedException e) {
//            // Handle the interrupted exception
//            e.printStackTrace();
//        }

        try {
            ConfigurableApplicationContext context = SpringApplication.run(ColumnStats.class, args);
            ShyneSparkUtils shyneSparkUtils = context.getBean(ShyneSparkUtils.class);
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");

            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);

            List<String> variables = Arrays.asList("DATASET_ID", "TRANSACTION_ID", "BRANCH_TYPE", "ENCODING", "PHYSICAL_ENDPOINT", "BRANCH", "RESULTS_ID", "COLUMN", "BUILD_TOKEN", "BUILD_TYPE", "BUILD_ID", "BOSLER_MOUNT_PATH", "BACKING_FS", "BOSLER_API");

            if (!Utils.checkEnvVariables(variables)) {
                throw new RuntimeException("Missing one or more required environment variables");
            }

            String datasetId = System.getenv("DATASET_ID");
            String branch = System.getenv("BRANCH");
            String column = System.getenv("COLUMN");
            String resultId = System.getenv("RESULTS_ID");
            String buildId = System.getenv("BUILD_ID");


            String transactionId = System.getenv("TRANSACTION_ID");
            ResourceSubtype branchType = ResourceSubtype.valueOf(System.getenv("BRANCH_TYPE"));
            String encoding = System.getenv("ENCODING");
            String physicalEndpoint = System.getenv("PHYSICAL_ENDPOINT");

            SchemaModel schemaModel = null;
            log.info(">>>> ENCODING : " + encoding);
            switch (branchType) {
                case JSON:
                case CSV:
                case XLS:
                case RAWDATASET: {
                    JsonNode schemaModelRaw = BosonApiCalls.getSchema(UUID.fromString(datasetId), branch, UUID.fromString(transactionId));
                    schemaModel = convertJsonNodeToClass(schemaModelRaw, SchemaModel.class);
                    break;
                }
                case PARQUET:
                case LIVEDATASET:
                case BUILDDATASET: {
                    break;
                }
                default:
                    throw new Exception("The dataset type is not supported : " + branchType);
            }

            Dataset<Row> dfTotal = ShyneSparkUtils.getShyneDF(UUID.fromString(datasetId), transactionId, branchType, encoding, schemaModel, -1, BuildType.DEFAULT, SparkUtils.createSparkSession(BuildType.DEFAULT), physicalEndpoint, null);
            ColumnStatsModel columnStatsModel = new ColumnStatsModel(UUID.fromString(datasetId), branch, UUID.fromString(transactionId), column);
            SparkResults sparkResults = SparkService.calculateStats(dfTotal, columnStatsModel, UUID.fromString(resultId));
            BosonApiCalls.sendSparkResults(sparkResults, UUID.fromString(buildId));
        } catch (Exception e) {
            logger.error("Column Stats failed", e.getMessage(), BuildType.valueOf(System.getenv("BUILD_TYPE")));
        }

    }
}

