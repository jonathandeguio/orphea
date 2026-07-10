package io.movetodata.build.shyne;

import com.fasterxml.jackson.databind.JsonNode;
import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.shyne.Utils.BosonApiCalls;
import io.movetodata.build.shyne.Utils.ShyneLogging;
import io.movetodata.build.shyne.Utils.Utils;
import io.movetodata.build.shyne.controller.k8s.PGSyncK8S;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.synchro.library.models.SyncSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.movetodata.build.shyne.Utils.Utils.convertJsonNodeToClass;

@Slf4j
@AllArgsConstructor
public class Synchro {
    static ShyneLogging logger = new ShyneLogging();
    static PGSyncK8S pgSyncK8S = new PGSyncK8S();

    public static void main(String[] args) throws Exception {
        try {
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");

            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);

            List<String> variables = Arrays.asList("DATASET_ID", "TRANSACTION_ID", "BRANCH_TYPE", "ENCODING", "PHYSICAL_ENDPOINT", "SYNC_ID", "BUILD_ID", "BUILD_TOKEN");

            if (!Utils.checkEnvVariables(variables)) {
                throw new RuntimeException("Missing one or more required environment variables");
            }

            UUID datasetId = UUID.fromString(System.getenv("DATASET_ID"));
            UUID transactionId = UUID.fromString(System.getenv("TRANSACTION_ID"));
            String branch = System.getenv("BRANCH");
            ResourceSubtype branchType = ResourceSubtype.valueOf(System.getenv("BRANCH_TYPE"));
            String encoding = System.getenv("ENCODING");
            String physicalEndpoint = System.getenv("PHYSICAL_ENDPOINT");
            UUID syncId = UUID.fromString(System.getenv("SYNC_ID"));
            UUID buildId = UUID.fromString(System.getenv("BUILD_ID"));
            boolean shallCreateIndexes = Boolean.parseBoolean(System.getenv("CREATE_INDEXES"));

            // Sync Specification
            SyncSpecification syncSpecification = null;
            JsonNode syncSpecificationRaw = BosonApiCalls.getSyncSpecification(syncId);
            syncSpecification = convertJsonNodeToClass(syncSpecificationRaw, SyncSpecification.class);

            // DatabaseSourceConfig
            DatabaseSourceConfig databaseSourceConfig = null;
            JsonNode databaseSourceConfigModelRaw = BosonApiCalls.getDatabaseSourceConfig(syncSpecification.getSourceId());
            databaseSourceConfig = convertJsonNodeToClass(databaseSourceConfigModelRaw, DatabaseSourceConfig.class);

            // SchemaModel
            SchemaModel schemaModel = null;
            log.info(">>>> ENCODING : " + encoding);

            switch (branchType) {
                case JSON:
                case CSV:
                case XLS:
                case RAWDATASET: {
                    JsonNode schemaModelRaw = BosonApiCalls.getSchema(datasetId, branch, transactionId);
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
            pgSyncK8S.performSync(databaseSourceConfig, syncSpecification, transactionId, branchType, encoding, schemaModel, physicalEndpoint, shallCreateIndexes, buildId);
            logger.finish("Sync success", "", BuildType.valueOf(System.getenv("BUILD_TYPE")));

        } catch (Exception e) {
            log.error(e.getMessage());
            logger.error("Sync failed", e.getMessage(), BuildType.valueOf(System.getenv("BUILD_TYPE")));
        }

    }
}

