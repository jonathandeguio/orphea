package io.movetodata.synchro.library.services;

import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.BobEnums.FunnelStatus;
import io.movetodata.build.library.services.BuildLogService;
import io.movetodata.build.library.services.K8Service;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.connect.library.services.JDBCService;
import io.movetodata.connect.library.services.SourceService;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.dataset.library.services.AsyncSparkService;
import io.movetodata.dataset.library.services.DatasetMappingService;
import io.movetodata.dataset.library.services.SparkDataService;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.services.BranchService;
import io.movetodata.platform.library.models.DataMartModel;
import io.movetodata.platform.library.models.SparkConfigModel;
import io.movetodata.platform.library.repository.SparkConfigRepository;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.Exceptions.DatabaseOperationException;
import io.movetodata.sharedutils.Utils;
import io.movetodata.synchro.DTOs.SyncPropertiesDTO;
import io.movetodata.synchro.library.exception.SyncException;
import io.movetodata.synchro.library.models.SyncIndex;
import io.movetodata.synchro.library.models.SyncSpecification;
import io.movetodata.synchro.library.repository.DataMartRepository;
import io.movetodata.synchro.library.repository.SyncIndexRepository;
import io.movetodata.synchro.library.repository.SyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static io.movetodata.sharedutils.Utils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SynchroService {
    private final SyncRepository syncRepository;
    private final SparkConfigRepository sparkConfigRepository;
    private final SyncIndexRepository syncIndexRepository;
    private final JDBCService jdbcService;
    private final BuildLogService buildLogService;
    private final DatasetMappingService datasetMappingService;
    private final SourceService sourceService;
    private final AsyncSparkService asyncSparkService;
    private final K8Service k8Service;
    private final BranchService branchService;
    private final SparkDataService sparkDataService;
    private final DataMartRepository dataMartRepository;
    private final PlatformConfigService platformConfigService;
    private final DataMartService dataMartService;

    public Optional<SyncSpecification> getSync(UUID syncId) {
        return syncRepository.findById(syncId);
    }

    public List<SyncSpecification> getDatasetSync(UUID datasetId, String branch) {
        if (platformConfigService.getPlatformConfig().getDataMartEnabled()) {
            List<SyncSpecification> syncSpecWithOnlyEnabledDataMartSyncSpecs = new ArrayList<>();
            List<SyncSpecification> specifications = syncRepository.findByDatasetIdAndBranch(datasetId, branch);
            for (SyncSpecification specification : specifications) {
                if (specification.isDataMartSyncSpec()) {
                    DataMartModel dataMartModel = dataMartRepository.findById(specification.getDataMartId()).orElse(null);
                    if (dataMartModel != null && dataMartModel.isEnabled()) {
                        syncSpecWithOnlyEnabledDataMartSyncSpecs.add(specification);
                    }
                } else {
                    syncSpecWithOnlyEnabledDataMartSyncSpecs.add(specification);
                }

            }
            return syncSpecWithOnlyEnabledDataMartSyncSpecs;
        } else {
            return syncRepository.findByDatasetIdAndBranchAndIsDataMartSyncSpec(datasetId, branch, false);
        }
    }

    public void manualSync(UUID syncId, UUID userId) throws Exception {
        Optional<SyncSpecification> syncSpec = getSync(syncId);
        if (syncSpec.isPresent()) {
            SyncSpecification syncSpecification = syncSpec.get();
            UUID transactionId = datasetMappingService.getCurrentTransaction(syncSpecification.getDatasetId(), syncSpecification.getBranch());
            UUID buildId = UUID.randomUUID();
            buildLogService.initialBuildLogSetupWithSockets(buildId, syncSpecification.getSourceId(), userId, BuildTrigger.SYNCHRO, BuildLaunchedBy.MANUAL, null, syncSpecification.getBranch());
            syncHandling(syncSpecification, BuildLaunchedBy.MANUAL, transactionId, userId, false, buildId);
        } else {
            throw new SyncException("No Sync Spec present");
        }
    }

    void handleDataMartSync(SyncSpecification syncSpec, UUID transactionId, boolean shallCreateIndexes, UUID buildId) throws Exception {
        DataMartModel dataMartModel = dataMartRepository.findById(syncSpec.getDataMartId()).get();
        Dataset<Row> sparkDF = sparkDataService.getSparkDF(syncSpec.getDatasetId(), syncSpec.getBranch(), transactionId, -1);

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", dataMartModel.getUsername());
        connectionProperties.put("password", dataMartModel.getPassword());

        buildLogService.updateBuildLog("Performing Sync on " + syncSpec.getDatasetId(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);
        buildLogService.updateBuildLog("Performing Sync to Data Mart " + dataMartModel.getName() + " to table : " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);

        sparkDF.write()
                .mode("overwrite")
                .jdbc("jdbc:postgresql://" + dataMartModel.getServer() + ":" + dataMartModel.getPort() + "/" + dataMartModel.getDatabase(),
                        "public." + syncSpec.getTableName(),
                        connectionProperties);
        buildLogService.updateBuildLog("Data written to POSTGRES table " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);
    }

    public void performSyncNew(UUID userId, SyncSpecification syncSpec, UUID transactionId, boolean shallCreateIndexes, UUID buildId) throws Exception {
        if (syncSpec.isDataMartSyncSpec()) {
            handleDataMartSync(syncSpec, transactionId, shallCreateIndexes, buildId);
            return;
        }
        DatabaseSourceConfig databaseSourceConfig = sourceService.getSourceDatabaseSourceConfig(syncSpec.getSourceId());
        Dataset<Row> sparkDF = sparkDataService.getSparkDF(syncSpec.getDatasetId(), syncSpec.getBranch(), transactionId, -1);

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", databaseSourceConfig.getUsername());
        connectionProperties.put("password", Utils.decodeBase64(databaseSourceConfig.getPassword()));

        buildLogService.updateBuildLog("Performing Sync on " + syncSpec.getDatasetId(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);
        buildLogService.updateBuildLog("Performing Sync to database " + databaseSourceConfig.getDbmsType() + " " + databaseSourceConfig.getDatabase() + " to table : " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);

        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                sparkDF.write()
                        .mode("overwrite")
                        .jdbc(JDBCService.JDBCUrl(databaseSourceConfig),
                                "public." + syncSpec.getTableName(),
                                connectionProperties);
                buildLogService.updateBuildLog("Data written to POSTGRES table " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);
                if (shallCreateIndexes) {
                    createIndexes(databaseSourceConfig, syncSpec, sparkDF, buildId);
                }
                break;
//            case MARIADB:
//                try {
////                    for testing -- if renaming works.
////                    sparkDF
////                            .withColumnRenamed("Index", "CustomerIndex");
//                    sparkDF.write()
//                            .mode("overwrite") // append, error is different with append.
//                            .jdbc(JDBCService.JDBCUrl(databaseSourceConfig),
//                                    syncSpec.getTableName(),
//                                    connectionProperties);
//                    buildLogService.updateBuildLog("Data written to MariaDB table " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);
//                } catch (Exception e) {
//                    log.error("Failed to write data to MariaDB table " + syncSpec.getTableName(), e);
//                    buildLogService.updateBuildLog("Failed to write data to MariaDB table " + syncSpec.getTableName(), FunnelStatus.ERROR, BuildStage.RUNNING, buildId);
//                    buildLogService.updateBuildLog(e.getMessage() + syncSpec.getTableName(), FunnelStatus.ERROR, BuildStage.RUNNING, buildId);
//                    throw e;
//                }
//
//                if (shallCreateIndexes) {
//                    createIndexes(databaseSourceConfig, syncSpec, sparkDF, buildId);
//                }
//                break;
            case MYSQL:
                sparkDF.write()
                        .mode("overwrite")
                        .jdbc(JDBCService.JDBCUrl(databaseSourceConfig),
                                syncSpec.getTableName(),
                                connectionProperties);
                buildLogService.updateBuildLog("Data written to MySQL table " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);

                if (shallCreateIndexes) {
                    createIndexes(databaseSourceConfig, syncSpec, sparkDF, buildId);
                }
                break;
            case SNOWFLAKE:
                Properties snowfalkeConnectionProperties = new Properties();
//                snowfalkeConnectionProperties.put("sfURL", JDBCService.JDBCUrl(databaseSourceConfig));
                snowfalkeConnectionProperties.put("user", databaseSourceConfig.getUsername());
                snowfalkeConnectionProperties.put("password", Utils.decodeBase64(databaseSourceConfig.getPassword()));
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
                        .mode("overwrite")
                        .jdbc(JDBCService.JDBCUrl(databaseSourceConfig),
                                syncSpec.getTableName(),
                                snowfalkeConnectionProperties);

                buildLogService.updateBuildLog("Data written to Snowflake table " + syncSpec.getTableName(), FunnelStatus.INFO, BuildStage.RUNNING, buildId);

                if (shallCreateIndexes) {
                    createIndexes(databaseSourceConfig, syncSpec, sparkDF, buildId);
                }
                break;
            default:
                throw new SyncException("Invalid JDBC type");
        }
    }

    public void syncHandling(SyncSpecification syncSpec, BuildLaunchedBy launchedBy, UUID transactionId, UUID userId, boolean shallCreateIndexes, UUID buildId) throws Exception {
        // Initiate a Build
        syncSpec.setUpdatedAt(new Date());
        syncRepository.save(syncSpec);
        SparkConfigModel sparkConfigModel = sparkConfigRepository.findByConfig("platform");
        if (Objects.equals(sparkConfigModel.getPgSync(), "kubernetes")) {
            BranchModel branchModel = branchService.getBranchModel(syncSpec.getDatasetId(), syncSpec.getBranch());

            SchemaModel schemaModel = sparkDataService.findByDatasetIdAndBranchAndTransactionIdAndStatus(syncSpec.getDatasetId(), syncSpec.getBranch(), transactionId);

            HashMap<String, String> envVars = k8Service.settingEnvVarsForDatabaseSync(syncSpec.getDatasetId(), syncSpec.getBranch(), transactionId, userId, buildId, schemaModel.getCustomSchema().getEncoding(), branchModel.getType(), syncSpec.getId(), shallCreateIndexes);
            asyncSparkService.asyncRunSparkSyncDatabaseJobWithKubernetes(envVars, userId, launchedBy);
        } else if (Objects.equals(sparkConfigModel.getPgSync(), "local")) {
            try {
                performSyncNew(userId, syncSpec, transactionId, shallCreateIndexes, buildId);
            } catch (Exception e) {
                buildLogService.updateBuildLog("Sync failed", FunnelStatus.ERROR, BuildStage.FINISHED, buildId, e.getMessage());
                log.error(e.getMessage());
            }

            buildLogService.updateBuildLog("Sync success", FunnelStatus.INFO, BuildStage.FINISHED, buildId);
        }

    }

    public void postTransformSyncHandling(UUID datasetId, String branch, UUID userId, UUID transactionId) throws Exception {
        // perform Sync
        List<SyncSpecification> syncSpecs = getDatasetSync(datasetId, branch);
        for (SyncSpecification syncSpec : syncSpecs) {
            // Only perform sync, if auto sync is true for syncSpec
            if (syncSpec.isAutoSyncOnBuild()) {
                UUID buildId = UUID.randomUUID();
                buildLogService.initialBuildLogSetupWithSockets(buildId, syncSpec.getSourceId(), userId, BuildTrigger.SYNCHRO, BuildLaunchedBy.TRANSFORM, null, syncSpec.getBranch());
                syncHandling(syncSpec, BuildLaunchedBy.TRANSFORM, transactionId, userId, false, buildId);
            }
        }
    }

    public void createIndexes(DatabaseSourceConfig databaseSourceConfig, SyncSpecification syncSpecification, Dataset<Row> df, UUID buildId) throws Exception {
        List<SyncIndex> indexes = syncSpecification.getSyncIndexes();
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                for (SyncIndex index : indexes) {
                    log.info("INDEX : " + index.toString());
                    List<String> columns = index.getColumns();
                    for (String column : columns) {
                        Optional<String> _col = Arrays.stream(df.columns()).filter(col -> col.equals(column)).findFirst();
                        if (_col.isEmpty()) {
                            throw new IllegalArgumentException("Column '" + column + "' not found in the DataFrame.");
                        }
                    }
                    String indexName = "index_" + syncSpecification.getTableName() + "_" + String.join("_", columns);
                    String tableName = syncSpecification.getTableName();
                    String columnsString = columns.stream()
                            .map(col -> "\"" + col + "\"")
                            .collect(Collectors.joining(","));
                    String query = String.format("CREATE INDEX %s ON %s (%s);", indexName, tableName, columnsString);


                    try {
                        log.info("Executing query: " + query);
                        jdbcService.executeJdbc(databaseSourceConfig, query, buildId);
                        buildLogService.updateBuildLog("Index Created", FunnelStatus.INFO, BuildStage.RUNNING, buildId);
                    } catch (Exception e) {
                        buildLogService.updateBuildLog(e.getMessage(), FunnelStatus.ERROR, BuildStage.FINISHED, buildId);
                        log.error("Failed to create index: {}", query, e);
                    }
                }
                break;
            case MYSQL:
                for (SyncIndex index : indexes) {
                    log.info("INDEX : " + index.toString());
                    List<String> columns = index.getColumns();
                    for (String column : columns) {
                        Optional<String> _col = Arrays.stream(df.columns()).filter(col -> col.equals(column)).findFirst();
                        if (_col.isEmpty()) {
                            throw new IllegalArgumentException("Column '" + column + "' not found in the Dataset.");
                        }
                    }
                    String indexName = "index_" + syncSpecification.getTableName() + "_" + String.join("_", columns);
                    String tableName = syncSpecification.getTableName();
                    List<String> columnsWithLength = new ArrayList<>();

                    for (String column : columns) {
                        if (isBlobOrTextColumn(df, column)) {
                            log.info("Column '" + column + "' is of type BLOB/TEXT. Adding key length.");
                            columnsWithLength.add("`" + column + "`(100)");
                        } else {
                            columnsWithLength.add("`" + column + "`");
                        }
                    }
                    String columnsString = String.join(",", columnsWithLength);
                    String query = String.format("CREATE INDEX `%s` ON `%s` (%s);", indexName, tableName, columnsString);
                    System.out.println(query);
                    try {
                        log.info("Executing query: {}", query);
                        jdbcService.executeJdbc(databaseSourceConfig, query, buildId);
                        buildLogService.updateBuildLog("Index Created", FunnelStatus.INFO, BuildStage.RUNNING, buildId);
                    } catch (Exception e) {
                        buildLogService.updateBuildLog(e.getMessage(), FunnelStatus.ERROR, BuildStage.FINISHED, buildId);
                        log.info("Failed to create index: {}", query, e);
                        throw e;
                    }
                }
                break;
            case SNOWFLAKE:
                log.info("Skipping index creation for Snowflake");
                break;
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    private boolean isBlobOrTextColumn(Dataset<?> dataset, String column) {
        StructType schema = dataset.schema();
        StructField field = schema.apply(column);
        String columnType = field.dataType().simpleString();
        log.info("Column '" + column + "' is of type: " + columnType);
        return columnType.equalsIgnoreCase("BLOB") || columnType.equalsIgnoreCase("TEXT");
    }

    public void verifyAndCreateTable(UUID sourceId, String tableName, UUID buildId) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = sourceService.getSourceDatabaseSourceConfig(sourceId);
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                if (!isPostgresTableNameValid(tableName)) {
                    throw new SyncException("Invalid Table Name");
                }
                String query = "CREATE TABLE " + tableName + " ()";
                jdbcService.executeJdbc(databaseSourceConfig, query, buildId);

                break;
//            case MARIADB:
//                if (!isMySQLTableNameValid(tableName)) {
//                    throw new SyncException("Invalid Table Name");
//                }
//                String mariadb_query = "CREATE TABLE `" + tableName + "` (id INT PRIMARY KEY)";
//                try {
//                    jdbcService.executeJdbc(databaseSourceConfig, mariadb_query, buildId);
//                    buildLogService.updateBuildLog("Table (" + tableName + ") created", FunnelStatus.INFO, BuildStage.RUNNING, buildId);
//                } catch (Exception e) {
//                    log.error("Failed to create table: " + mariadb_query, e);
//                    buildLogService.updateBuildLog("Failed to create table: " + mariadb_query, FunnelStatus.ERROR, BuildStage.RUNNING, buildId);
//                    throw e;
//                }
//                break;
            case MYSQL:
                if (!isMySQLTableNameValid(tableName)) {
                    throw new SyncException("Invalid Table Name");
                }
                String mysql_query = "CREATE TABLE " + tableName + " (id INT PRIMARY KEY)";
                jdbcService.executeJdbc(databaseSourceConfig, mysql_query, buildId);

                break;
            case SNOWFLAKE:
                if (!isSnowflakeTableNameValid(tableName)) {
                    throw new SyncException("Invalid Table Name");
                }
                String snowflake_query = "CREATE TABLE " + tableName + " (id INT PRIMARY KEY)";
                jdbcService.executeJdbc(databaseSourceConfig, snowflake_query, buildId);

                break;
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    public void renameDatabaseTable(UUID sourceId, String oldTableName, String newTableName, UUID buildId) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = sourceService.getSourceDatabaseSourceConfig(sourceId);

        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                String query = "ALTER TABLE " + oldTableName + " RENAME TO " + newTableName;
                jdbcService.executeJdbc(databaseSourceConfig, query, buildId);

                break;
//            case MARIADB:
            case MYSQL:
                String query2 = "RENAME TABLE " + oldTableName + " TO " + newTableName;
                jdbcService.executeJdbc(databaseSourceConfig, query2, buildId);

                break;
            case SNOWFLAKE:
                String snowflakeQuery = "ALTER TABLE " + oldTableName + " RENAME TO " + newTableName;
                jdbcService.executeJdbc(databaseSourceConfig, snowflakeQuery, buildId);

                break;
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    public void renameDataMartTable(UUID dataMartId, String oldTableName, String newTableName, UUID buildId) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = dataMartService.getDatabaseSourceConfig(dataMartId);
        String query = "ALTER TABLE " + oldTableName + " RENAME TO " + newTableName;
        jdbcService.executeJdbc(databaseSourceConfig, query, buildId);
    }

    public void deleteDatabaseTable(UUID sourceId, String tableName, UUID buildId) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = sourceService.getSourceDatabaseSourceConfig(sourceId);

        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                String query = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
                jdbcService.executeJdbc(databaseSourceConfig, query, buildId);
                break;
//            case MARIADB:
            case MYSQL:
                String mysql_query = "DROP TABLE IF EXISTS " + tableName;
                jdbcService.executeJdbc(databaseSourceConfig, mysql_query, buildId);
                break;
            case SNOWFLAKE:
                String snowflake_query = "DROP TABLE IF EXISTS " + tableName;
                jdbcService.executeJdbc(databaseSourceConfig, snowflake_query, buildId);
                break;
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    public List<SyncIndex> createSyncIndexes(List<List<String>> indexes, UUID userId) {
        List<SyncIndex> syncIndexes = new ArrayList<>();

        for (List<String> columns : indexes) {
            SyncIndex syncIndex = new SyncIndex();
            syncIndex.setColumns(columns);
            syncIndex.setCreatedBy(userId);
            syncIndex.setCreatedAt(new Date());
            syncIndex.setUpdatedBy(userId);
            syncIndex.setUpdatedAt(new Date());

            syncIndexes.add(syncIndexRepository.save(syncIndex));
        }

        return syncIndexes;
    }

    public void verifyAndCreateTableDataMart(UUID dataMartId, String tableName, UUID buildId) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = dataMartService.getDatabaseSourceConfig(dataMartId);
        if (!isPostgresTableNameValid(tableName)) {
            throw new SyncException("Invalid Table Name");
        }
        String query = "CREATE TABLE " + tableName + " ()";
        jdbcService.executeJdbc(databaseSourceConfig, query, buildId);
    }

    public void deleteDatabaseTableDataMart(SyncSpecification syncSpecification, String oldTableName, UUID buildId) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = dataMartService.getDatabaseSourceConfig(syncSpecification.getDataMartId());
        String query = "DROP TABLE IF EXISTS " + syncSpecification.getTableName() + " CASCADE";
        jdbcService.executeJdbc(databaseSourceConfig, query, buildId);
    }

    public SyncSpecification putSync(SyncPropertiesDTO syncProperties, UUID userId) throws Exception {
        UUID datasetId = syncProperties.getDatasetId();
        String branch = syncProperties.getBranch();
        UUID sourceId = syncProperties.getSourceId();
        String tableName = syncProperties.getTableName();
        UUID syncId = syncProperties.getId();

        boolean isAutoSyncOnBuild = syncProperties.isAutoSyncOnBuild();
        UUID transactionId = datasetMappingService.getCurrentTransaction(datasetId, branch);
        UUID buildId = UUID.randomUUID();
        buildLogService.initialBuildLogSetupWithSockets(buildId, syncProperties.getSourceId(), userId, BuildTrigger.SYNCHRO, BuildLaunchedBy.MANUAL, null, branch);

        if (syncId != null && getSync(syncId).isPresent()) {
            SyncSpecification alreadyExistedSyncSpec = getSync(syncId).get();
            String oldTable = alreadyExistedSyncSpec.getTableName();
            UUID oldSourceId = alreadyExistedSyncSpec.getSourceId();
            try {
                if (syncProperties.getIsDataMartSyncSpec()) {
                    renameDataMartTable(alreadyExistedSyncSpec.getDataMartId(),alreadyExistedSyncSpec.getTableName(),syncProperties.getTableName(),buildId);
                } else {
                    deleteDatabaseTable(oldSourceId, oldTable, buildId);
                }
            } catch (Exception e) {
                buildLogService.updateBuildLog(e.getMessage(), FunnelStatus.ERROR, BuildStage.FINISHED, buildId);
                throw new SyncException(e.getMessage());
            }

            try {
                if (syncProperties.getIsDataMartSyncSpec()) {
                    verifyAndCreateTableDataMart(alreadyExistedSyncSpec.getDataMartId(), alreadyExistedSyncSpec.getTableName(), buildId);
                } else {
                    verifyAndCreateTable(sourceId, tableName, buildId);
                }

            } catch (Exception e) {
                buildLogService.updateBuildLog(e.getMessage(), FunnelStatus.ERROR, BuildStage.FINISHED, buildId);
                throw new SyncException(e.getMessage());
            }

            alreadyExistedSyncSpec.setSyncIndexes(createSyncIndexes(syncProperties.getSyncIndexes(), userId));
            alreadyExistedSyncSpec.setTableName(tableName);
            alreadyExistedSyncSpec.setUpdatedAt(new Date());
            alreadyExistedSyncSpec.setUpdatedBy(userId);
            alreadyExistedSyncSpec.setAutoSyncOnBuild(isAutoSyncOnBuild);
            SyncSpecification _syncSpec = syncRepository.save(alreadyExistedSyncSpec);
            syncHandling(_syncSpec, BuildLaunchedBy.MANUAL, transactionId, userId, true, buildId);

            return _syncSpec;
        } else {
            try {
                if (syncProperties.getIsDataMartSyncSpec()) {
                    verifyAndCreateTableDataMart(syncProperties.getDataMartId(), syncProperties.getTableName(), buildId);
                } else {
                    verifyAndCreateTable(sourceId, tableName, buildId);
                }
            } catch (Exception e) {
                buildLogService.updateBuildLog(e.getMessage(), FunnelStatus.ERROR, BuildStage.FINISHED, buildId);
                throw new SyncException(e.getMessage());
            }


            SyncSpecification syncSpecification = new SyncSpecification();
            syncSpecification.setDatasetId(datasetId);
            syncSpecification.setBranch(branch);
            syncSpecification.setSourceId(sourceId);

            syncSpecification.setSyncIndexes(createSyncIndexes(syncProperties.getSyncIndexes(), userId));
            syncSpecification.setTableName(tableName);
            syncSpecification.setAutoSyncOnBuild(isAutoSyncOnBuild);

            syncSpecification.setCreatedAt(new Date());
            syncSpecification.setCreatedBy(userId);

            syncSpecification.setUpdatedAt(new Date());
            syncSpecification.setUpdatedBy(userId);

            if (syncProperties.getIsDataMartSyncSpec()) {
                syncSpecification.setAutoSyncOnBuild(true);
                syncSpecification.setDataMartId(syncProperties.getDataMartId());
                syncSpecification.setDataMartSyncSpec(true);
            }

            SyncSpecification _syncSpec = syncRepository.save(syncSpecification);
            syncHandling(_syncSpec, BuildLaunchedBy.MANUAL, transactionId, userId, true, buildId);

            return _syncSpec;
        }
    }

    public void deleteSync(UUID syncId) throws Exception {
        Optional<SyncSpecification> syncSpec = getSync(syncId);
        if (syncSpec.isPresent()) {
            syncSpec.ifPresent(syncRepository::delete);

            deleteDatabaseTable(syncSpec.get().getSourceId(), syncSpec.get().getTableName(), null);
        } else {
            throw new SyncException("Sync not present");
        }

    }
}
