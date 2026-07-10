package io.movetodata.build.shyne.controller.k8s;

import com.fasterxml.jackson.databind.JsonNode;
import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.shyne.Utils.BosonApiCalls;
import io.movetodata.build.shyne.Utils.ShyneLogging;
import io.movetodata.build.shyne.Utils.ShyneSparkUtils;
import io.movetodata.build.shyne.Utils.SynchroUtils;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.platform.library.models.DataMartModel;
import io.movetodata.sharedutils.Exceptions.DatabaseOperationException;
import io.movetodata.sharedutils.SparkUtils;
import io.movetodata.synchro.library.exception.SyncException;
import io.movetodata.synchro.library.models.SyncIndex;
import io.movetodata.synchro.library.models.SyncSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.jvnet.hk2.annotations.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static io.movetodata.build.shyne.Utils.Utils.convertJsonNodeToClass;

@Slf4j
@Service
@RequiredArgsConstructor
public class PGSyncK8S {
    static ShyneLogging logger = new ShyneLogging();
    Connection connection = null;

    public void executePostgres(DatabaseSourceConfig databaseSourceConfig, String query, UUID buildId) throws Exception {
        connection = DriverManager.getConnection(SynchroUtils.JDBCUrl(databaseSourceConfig));

        try {
            Statement st = connection.createStatement();
            st.execute(query);
        } catch (SQLException e) {
            logger.error(e.getMessage(), query, BuildType.DEFAULT);
            throw new DatabaseOperationException(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), query, BuildType.DEFAULT);
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void createIndexes(DatabaseSourceConfig databaseSourceConfig, SyncSpecification syncSpecification, Dataset<Row> df, UUID buildId) throws Exception {
        List<SyncIndex> indexes = syncSpecification.getSyncIndexes();
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                for (SyncIndex index : indexes) {
                    List<String> columns = index.getColumns();
                    for (String column : columns) {
                        Optional<String> _col = Arrays.stream(df.columns()).findFirst();
                        if (_col.isEmpty()) {
                            throw new IllegalArgumentException("Column '" + column + "' not found in the DataFrame.");
                        }
                    }

                    // Construct the CREATE INDEX query
                    String indexName = "index_" + syncSpecification.getTableName() + "_" + String.join("_", columns);
                    String tableName = syncSpecification.getTableName();
                    String columnsString = '"' + String.join("\",\"", columns) + '"';
                    String query = String.format("CREATE INDEX %s ON %s (%s);", indexName, tableName, columnsString);


                    executePostgres(databaseSourceConfig, query, buildId);
                    logger.info("Index Created", query, BuildType.DEFAULT);
                }
                break;
            case SNOWFLAKE:
                log.info("Skipping index creation for Snowflake");
                break;
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    public void handleDataMartSync(SyncSpecification syncSpec, UUID transactionId, ResourceSubtype branchType, String encoding, SchemaModel schemaModel, String physicalEndpoint, boolean shallCreateIndexes, UUID buildId) throws Exception {
        JsonNode dataMartModelRaw = BosonApiCalls.getDataMartModel(syncSpec.getDataMartId());
        DataMartModel dataMartModel = convertJsonNodeToClass(dataMartModelRaw, DataMartModel.class);
        Dataset<Row> sparkDF = ShyneSparkUtils.getShyneDF(syncSpec.getDatasetId(), String.valueOf(transactionId), branchType, encoding, schemaModel, -1, BuildType.DEFAULT, SparkUtils.createSparkSession(BuildType.DEFAULT), physicalEndpoint, null);

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", dataMartModel.getUsername());
        connectionProperties.put("password", dataMartModel.getPassword());

        logger.info("Performing Sync on " + syncSpec.getDatasetId(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));
        logger.info("Performing Sync to database " + dataMartModel.getDatabase() + " to table : " + syncSpec.getTableName(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));


        sparkDF.write()
                .mode("overwrite")
                .jdbc("jdbc:postgresql://" + dataMartModel.getServer() + ":" + dataMartModel.getPort() + "/" + dataMartModel.getDatabase(),
                        "public." + syncSpec.getTableName(),
                        connectionProperties);
        logger.info("Data written to " + syncSpec.getTableName(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));
    }

    public void performSync(DatabaseSourceConfig databaseSourceConfig, SyncSpecification syncSpec, UUID transactionId, ResourceSubtype branchType, String encoding, SchemaModel schemaModel, String physicalEndpoint, boolean shallCreateIndexes, UUID buildId) throws Exception {
        if (syncSpec.isDataMartSyncSpec()) {
            handleDataMartSync(syncSpec, transactionId, branchType, encoding, schemaModel, physicalEndpoint, shallCreateIndexes, buildId);
            return;
        }
        Dataset<Row> sparkDF = ShyneSparkUtils.getShyneDF(syncSpec.getDatasetId(), String.valueOf(transactionId), branchType, encoding, schemaModel, -1, BuildType.DEFAULT, SparkUtils.createSparkSession(BuildType.DEFAULT), physicalEndpoint, null);

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", databaseSourceConfig.getUsername());
        connectionProperties.put("password", SynchroUtils.decodeBase64(databaseSourceConfig.getPassword()));

        logger.info("Performing Sync on " + syncSpec.getDatasetId(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));
        logger.info("Performing Sync to database " + databaseSourceConfig.getDbmsType() + " " + databaseSourceConfig.getDatabase() + " to table : " + syncSpec.getTableName(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));

        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                sparkDF.write()
                        .mode("overwrite")
                        .jdbc(SynchroUtils.JDBCUrl(databaseSourceConfig),
                                "public." + syncSpec.getTableName(),
                                connectionProperties);
                if (shallCreateIndexes) {
                    createIndexes(databaseSourceConfig, syncSpec, sparkDF, buildId);
                }
                logger.info("Data written to POSTGRES table " + syncSpec.getTableName(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));
                break;
            case SNOWFLAKE:
                Properties snowfalkeConnectionProperties = new Properties();
//                snowfalkeConnectionProperties.put("sfURL", JDBCService.JDBCUrl(databaseSourceConfig));
                snowfalkeConnectionProperties.put("user", databaseSourceConfig.getUsername());
                snowfalkeConnectionProperties.put("password", SynchroUtils.decodeBase64(databaseSourceConfig.getPassword()));
//                snowfalkeConnectionProperties.put("database", databaseSourceConfig.getDatabase());
//                snowfalkeConnectionProperties.put("schema", databaseSourceConfig.getSchema());
//                snowfalkeConnectionProperties.put("warehouse", databaseSourceConfig.getWarehouse());
//                snowfalkeConnectionProperties.put("role", databaseSourceConfig.getUserRole());
//                snowfalkeConnectionProperties.put("dbtable", syncSpec.getTableName());
//                snowfalkeConnectionProperties.put("isolationLevel", "NONE");

                snowfalkeConnectionProperties.put("sfDebug", "true");

                // Note below either use batch size or copy into
                // BATCH Size
                snowfalkeConnectionProperties.put("use_copy_unload", "false");
//                snowfalkeConnectionProperties.put("batch_size", "100000");
                snowfalkeConnectionProperties.put("batchsize", "500000");

                // COPY INTO
//                snowfalkeConnectionProperties.put("use_copy_unload", "true");
//                snowfalkeConnectionProperties.put("sfChunkSize", "512"); // Optional: Adjust chunk size


//                snowfalkeConnectionProperties.put("use_copy_unload", "true"); // Enable COPY INTO for faster data writes
//                snowfalkeConnectionProperties.put("truncate_table", "false");
//                connectionProperties.put("batchsize", "10000");
//                connectionProperties.put("use_copy_unload", "true");

                sparkDF.write()
//                        .format("net.snowflake.spark.snowflake")
                        .mode("overwrite")
                        .jdbc(SynchroUtils.JDBCUrl(databaseSourceConfig),
                                syncSpec.getTableName(),
                                snowfalkeConnectionProperties);
                logger.info("Data written to Snowflake table " + syncSpec.getTableName(), "", BuildType.valueOf(System.getenv("BUILD_TYPE")));
                if (shallCreateIndexes) {
                    createIndexes(databaseSourceConfig, syncSpec, sparkDF, buildId);
                }
                break;
            default:
                throw new SyncException("Invalid JDBC type");
        }
    }
}
