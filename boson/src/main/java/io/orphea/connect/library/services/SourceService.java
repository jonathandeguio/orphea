package io.orphea.connect.library.services;

import io.orphea.connect.library.DTOs.SourceSQLTableExplanation;
import io.orphea.connect.library.enums.SourceTypeEnum;
import io.orphea.connect.library.models.*;
import io.orphea.connect.library.repository.*;
import io.orphea.connect.library.requests.SourceRequestDTO;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.sharedutils.Exceptions.DatabaseOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceService {
    private final FolderSourceConfigRepository folderSourceConfigRepository;
    private final SourcesRepository sourcesRepository;
    private final AgentRepository agentRepository;
    private final ConnectConfigRepository connectConfigRepository;
    private final ResourceService resourceService;
    private final JDBCService jdbcService;
    private final io.orphea.connect.library.services.DatabaseSourceConfigService databaseSourceConfigService;
    private final FolderSourceConfigService folderSourceConfigService;
    private final DatabaseSourceConfigRepository databaseSourceConfigRepository;
    private final RestSourceConfigRepository restSourceConfigRepository;
    private final RestSourceDomainRepository restSourceDomainRepository;
    private final SharepointConfigService sharepointConfigService;

    public Source findById(UUID id) {
        return sourcesRepository.findById(id).orElseThrow(() -> new NoSuchElementException(MessageFormat.format("source with Id {0} does not exist", id)));
    }

    public boolean existsById(UUID id) {
        return sourcesRepository.existsById(id);
    }


    public Source getSource(UUID id) {
        return sourcesRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(MessageFormat.format("connectors with Id {0} does not exist", id)));
    }

    @NotNull
    public List<Source> getSources(UUID userId) {
        return resourceService
                .getActiveResources(userId, ResourceType.SOURCE)
                .parallelStream()
                .map(resourceModel -> sourcesRepository.findById(resourceModel.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void deleteSourceById(UUID id) {
        Source source = getSource(id);

        if (!source.isDirectLoad()) {
            updateConfig(source.getAgentId());
        }

        sourcesRepository.deleteById(id);
        resourceService.deleteById(id);

    }

    @Transactional
    public Source createNewSource(SourceRequestDTO sourceRequest, UUID userId) throws IOException {
        if (!resourceService.existsById(sourceRequest.getParent())) {
            throw new NoSuchElementException(MessageFormat.format("connectors with Id {0} does not exist", sourceRequest.getParent()));
        }

        if (sourceRequest.getAgentId().parallelStream().anyMatch(agentId -> !agentRepository.existsById(agentId))) {
            throw new NoSuchElementException("Agent with Id " + sourceRequest.getAgentId() + " does not exist");
        }

        // Create in kitab
        // TODO: Subtype add
        ResourceModel resourceModel = resourceService.newResource(sourceRequest.getName(), sourceRequest.getDescription(), ResourceType.SOURCE, ResourceSubtype.NONE, userId, sourceRequest.getParent());
        Source source = Source.builder()
                .id(resourceModel.getId())
                .type(sourceRequest.getType())
                .directLoad(sourceRequest.isDirectLoad())
                .agentId(sourceRequest.getAgentId())
                .build();


        if (Objects.equals(source.getType(), "jdbc")) {
            DatabaseSourceConfig databaseSourceConfig = DatabaseSourceConfig.builder()
                    .username(sourceRequest.getUsername())
                    .password(sourceRequest.getPassword())
                    .database(sourceRequest.getDatabase())
                    .server(sourceRequest.getServer())
                    .port(sourceRequest.getPort())
                    .warehouse(sourceRequest.getWarehouse())
                    .authType(sourceRequest.getAuthType())
                    .schema(sourceRequest.getSchema())
                    .userRole(sourceRequest.getUserRole())
                    .privateKey(sourceRequest.getPrivateKey())
                    .privateKeyPassPhrase(sourceRequest.getPrivateKeyPassPhrase())
                    .dbmsType(sourceRequest.getDbmsType()).build();
            DatabaseSourceConfig savedDatabaseSourceConfig = databaseSourceConfigService.save(databaseSourceConfig);
            source.setSourceConfig(savedDatabaseSourceConfig.getId());

        } else if (Objects.equals(source.getType().toUpperCase(), "FOLDER")) {
            FolderSourceConfig folderSourceConfig = FolderSourceConfig.builder()
                    .path(sourceRequest.getPath()).build();

            FolderSourceConfig savedFolderSourceConfig = folderSourceConfigService.save(folderSourceConfig);
            source.setSourceConfig(savedFolderSourceConfig.getId());
        } else if (Objects.equals(source.getType(), "rest")) {
            List<RestAPISourceDomain> domains = sourceRequest.getDomains().stream().map(dto ->
                    RestAPISourceDomain.builder().domain(dto.getDomain())
                            .protocol(dto.getProtocol())
                            .apiKeyValue(dto.getApiKeyValue())
                            .apiKeyName(dto.getApiKeyName())
                            .bearerToken(dto.getBearerToken())
                            .port(dto.getPort())
                            .authType(dto.getAuthType())
                            .build()).collect(Collectors.toList());

            RestAPISourceConfig restAPISourceConfig = RestAPISourceConfig.builder().domains(domains).build();
            RestAPISourceConfig savedRestAPISourceConfig = restSourceConfigRepository.save(restAPISourceConfig);
            source.setSourceConfig(savedRestAPISourceConfig.getId());
        } else if ("sharepoint".equalsIgnoreCase(sourceRequest.getType())) {
            SharePointSourceConfig sharePointSourceConfig = SharePointSourceConfig.builder().tenantId(sourceRequest.getTenantId()).clientSecret(sourceRequest.getClientSecret()).clientId(sourceRequest.getClientId()).url(sourceRequest.getUrl()).build();
            SharePointSourceConfig savedSharePointSourceConfig = sharepointConfigService.save(sharePointSourceConfig);
            source.setSourceConfig(savedSharePointSourceConfig.getId());
        }

        Source source1 = sourcesRepository.save(source);

        if (!source.isDirectLoad()) {
            updateConfig(source1.getAgentId());
        }
        return source1;
    }

    @Transactional
    public Source updateSource(SourceRequestDTO sourceRequestDTO, UUID userId) throws IOException {
        if (sourceRequestDTO.getParent() != null) {
            if (!resourceService.existsById(sourceRequestDTO.getParent())) {
                throw new NoSuchElementException(MessageFormat.format("parent with Id {0} does not exist", sourceRequestDTO.getParent()));
            }
            if (sourceRequestDTO.getAgentId().parallelStream().anyMatch(agentId -> !agentRepository.existsById(agentId))) {
                throw new NoSuchElementException(MessageFormat.format("Agent with Id {0} does not exist", sourceRequestDTO.getAgentId()));
            }
        }

        Source source = sourcesRepository.getReferenceById(sourceRequestDTO.getId());
        ResourceModel folder = resourceService.findById(sourceRequestDTO.getId()).orElseThrow();
        folder.copyNonNullProperties(sourceRequestDTO);
        if (Objects.equals(sourceRequestDTO.getType(), "jdbc")) {
            DatabaseSourceConfig databaseSourceConfig = new DatabaseSourceConfig();
            if (Objects.equals(sourceRequestDTO.getType(), source.getType())) {
                databaseSourceConfig = databaseSourceConfigService.findById(sourceRequestDTO.getSourceConfig());
            }

            if (sourceRequestDTO.getDbmsType() != null) {
                databaseSourceConfig.setDbmsType(sourceRequestDTO.getDbmsType());
            }
            if (sourceRequestDTO.getDatabase() != null) {
                databaseSourceConfig.setDatabase(sourceRequestDTO.getDatabase());
            }
            if (sourceRequestDTO.getUsername() != null) {
                databaseSourceConfig.setUsername(sourceRequestDTO.getUsername());
            }
            if (sourceRequestDTO.getPassword() != null) {
                databaseSourceConfig.setPassword(sourceRequestDTO.getPassword());
            }
            if (sourceRequestDTO.getServer() != null) {
                databaseSourceConfig.setServer(sourceRequestDTO.getServer());
            }
            if (sourceRequestDTO.getPort() != null) {
                databaseSourceConfig.setPort(sourceRequestDTO.getPort());
            }
            if (sourceRequestDTO.getWarehouse() != null) {
                databaseSourceConfig.setWarehouse(sourceRequestDTO.getWarehouse());
            }
            if (sourceRequestDTO.getAuthType() != null) {
                databaseSourceConfig.setAuthType(sourceRequestDTO.getAuthType());
            }
            if (sourceRequestDTO.getSchema() != null) {
                databaseSourceConfig.setSchema(sourceRequestDTO.getSchema());
            }
            if (sourceRequestDTO.getUserRole() != null) {
                databaseSourceConfig.setUserRole(sourceRequestDTO.getUserRole());
            }
            if (sourceRequestDTO.getWarehouse() != null) {
                databaseSourceConfig.setWarehouse(sourceRequestDTO.getWarehouse());
            }
            if (sourceRequestDTO.getPrivateKey() != null) {
                databaseSourceConfig.setPrivateKey(sourceRequestDTO.getPrivateKey());
            }
            if (sourceRequestDTO.getPrivateKeyPassPhrase() != null) {
                databaseSourceConfig.setPrivateKeyPassPhrase(sourceRequestDTO.getPrivateKeyPassPhrase());
            }

            databaseSourceConfigRepository.save(databaseSourceConfig);
            source.setSourceConfig(databaseSourceConfig.getId());

        } else if (Objects.equals(sourceRequestDTO.getType(), "FOLDER")) {
            FolderSourceConfig folderSourceConfig = new FolderSourceConfig();
            if (Objects.equals(sourceRequestDTO.getType(), source.getType())) {
                folderSourceConfig = folderSourceConfigService.findById(sourceRequestDTO.getSourceConfig());
            }

            if (sourceRequestDTO.getPath() != null) {
                folderSourceConfig.setPath(sourceRequestDTO.getPath());
            }
            folderSourceConfigRepository.save(folderSourceConfig);
            source.setSourceConfig(folderSourceConfig.getId());
        } else if (Objects.equals(sourceRequestDTO.getType(), "rest")) {
            List<RestAPISourceDomain> domains = sourceRequestDTO.getDomains().stream().map(dto ->
                    RestAPISourceDomain.builder().id(dto.getId()).domain(dto.getDomain())
                            .protocol(dto.getProtocol())
                            .apiKeyValue(dto.getApiKeyValue())
                            .apiKeyName(dto.getApiKeyName())
                            .bearerToken(dto.getBearerToken())
                            .port(dto.getPort())
                            .authType(dto.getAuthType())
                            .build()).collect(Collectors.toList());

            RestAPISourceConfig restAPISourceConfig = restSourceConfigRepository.findById(source.getSourceConfig()).orElseThrow();
            restAPISourceConfig.setDomains(domains);
            restSourceConfigRepository.save(restAPISourceConfig);
        } else if (Objects.equals(sourceRequestDTO.getType(), "SHAREPOINT")) {
            SharePointSourceConfig sharePointSourceConfig = new SharePointSourceConfig();
            if (Objects.equals(sourceRequestDTO.getType(), source.getType())) {
                sharePointSourceConfig = sharepointConfigService.findById(sourceRequestDTO.getSourceConfig());
            }

            if (sourceRequestDTO.getTenantId() != null) {
                sharePointSourceConfig.setTenantId(sourceRequestDTO.getTenantId());
            }
            if (sourceRequestDTO.getClientId() != null) {
                sharePointSourceConfig.setClientId(sourceRequestDTO.getClientId());
            }
            if (sourceRequestDTO.getClientSecret() != null) {
                sharePointSourceConfig.setClientSecret(sourceRequestDTO.getClientSecret());
            }
            if (sourceRequestDTO.getUrl() != null) {
                sharePointSourceConfig.setUrl(sourceRequestDTO.getUrl());
            }
            sharepointConfigService.save(sharePointSourceConfig);
        } else {
            source.setSourceConfig(sourceRequestDTO.getSourceConfig());
        }
        if (sourceRequestDTO.getType() != null) {
            source.setType(sourceRequestDTO.getType());
        }

        resourceService.save(folder);
        Source source1 = sourcesRepository.save(source);
        if (!source.isDirectLoad()) {
            // update config Status
            updateConfig(source1.getAgentId());
        }
        return source1;
    }

    @NotNull
    public Map<String, String> testJdbcConnection(DatabaseSourceConfig testConnection) throws Exception {
        // Get the system's default timezone
        TimeZone timeZone = TimeZone.getDefault();
        TimeZone.setDefault(timeZone);

        if (testConnection.getDbmsType().equals(SourceTypeEnum.ORACLE21)) {
            System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        }

        HashMap<String, String> message = new HashMap<>();

        try (Connection connection = jdbcService.getJdbcConnection(testConnection)) {
            message.put("status", "SUCCESS");
            message.put("message", "Connection to database established successfully.");
        } catch (SQLException e) {
            message.put("status", "FAILED");
            message.put("message", "Error : " + e.getMessage());
        }
        return message;
    }


    @NotNull
    public Map<String, Object> checkFolderPathExists(TestConnection testConnection) {
        HashMap<String, Object> message = new HashMap<>();

        String sourcePath = testConnection.getPath();

        // Check if sourcePath ends with a slash and remove it
        if (sourcePath.endsWith("/")) {
            sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        }

        File folder = new File(sourcePath);

        if (folder.exists() && folder.isDirectory()) {
            message.put("status", true);
            message.put("message", "Folder found in the filesystem.");
        } else {
            message.put("status", false);
            message.put("message", "Error : folder not found");
        }
        return message;
    }

    public void updateConfig(List<UUID> agentIdList) {
        for (UUID agentId : agentIdList) {
            ConnectConfig connectConfig = new ConnectConfig();
            connectConfig.setAgentId(agentId);
            connectConfig.setVersion(UUID.randomUUID());
            connectConfig.setUpdatedAt(new Date());
            connectConfigRepository.save(connectConfig);
        }
    }

    public DatabaseSourceConfig getSourceDatabaseSourceConfig(UUID sourceId) {
        UUID databaseSourceConfigId = getSource(sourceId).getSourceConfig();
        return databaseSourceConfigService.findById(databaseSourceConfigId);
    }

    public Optional<FolderSourceConfig> getFolderSourceConfig(UUID sourceId) {
        UUID folderSourceConfigId = getSource(sourceId).getSourceConfig();
        return folderSourceConfigRepository.findById(folderSourceConfigId);
    }

    public Optional<RestAPISourceConfig> getRestAPISourceConfig(UUID sourceId) {
        return restSourceConfigRepository.findById(getSource(sourceId).getSourceConfig());
    }

    public ResourceType getResourceType(SourceTypeEnum source) {
        switch (source) {
            case POSTGRES:
                return ResourceType.POSTGRESSOURCE;
            case MARIADB:
                return ResourceType.MARIASOURCE;
            case MYSQL:
                return ResourceType.MYSQLSOURCE;
            case MSSQLSERVER:
                return ResourceType.MYSQLSERVERSOURCE;
            case ORACLE21:
                return ResourceType.ORACLE21SOURCE;
            case SNOWFLAKE:
                return ResourceType.SNOWFLAKESOURCE;
            case FILESYSTEM:
                return ResourceType.FILESYSTEMSOURCE;
            default:
                throw new DatabaseOperationException("Not a valid Source type");
        }
    }

    public SourceSQLTableExplanation explainSQLTable(UUID sourceId, String tableName) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = getSourceDatabaseSourceConfig(sourceId);
        SourceSQLTableExplanation sourceSQLTableExplanation = new SourceSQLTableExplanation();
//        sourceSQLTableExplanation.setColumns(getSqlColumns(databaseSourceConfig, tableName));
        List<List<String>> result = getSqlMetaData(databaseSourceConfig, tableName).getResults().get(0).getData();
        if (!result.isEmpty()) {
            sourceSQLTableExplanation.setRowsCount(result.get(0).get(1));
            if (sourceSQLTableExplanation.getRowsCount().equals("-1")) {
                sourceSQLTableExplanation.setRowsCount(fallbackRowsCount(databaseSourceConfig, tableName, sourceSQLTableExplanation.getRowsCount()));
            }
            sourceSQLTableExplanation.setTableSize(result.get(0).get(2));
            sourceSQLTableExplanation.setCreatedAt(result.get(0).get(3));
            sourceSQLTableExplanation.setUpdatedAt(result.get(0).get(4));
        }

        sourceSQLTableExplanation.setDdlQuery(getSqlDDLQuery(databaseSourceConfig, tableName));
        return sourceSQLTableExplanation;
    }

    public List<List<String>> getSqlColumns(UUID sourceId, String tableName) throws Exception {
        DatabaseSourceConfig databaseSourceConfig = getSourceDatabaseSourceConfig(sourceId);
        Results columnsResult = getSourceTableSchema(databaseSourceConfig, tableName);
        return new ArrayList<>(columnsResult.getResults().get(0).getData());
    }

    public Results getTableDefinition(DatabaseSourceConfig databaseSourceConfig, String tableSchema, String tableName) throws Exception {
        String query = null;
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                query = String.format(
                        "WITH column_info AS (\n" +
                                "    SELECT \n" +
                                "        column_name,\n" +
                                "        CASE\n" +
                                "            WHEN data_type = 'character varying' THEN data_type || '(' || character_maximum_length || ')'\n" +
                                "            WHEN data_type = 'character' THEN data_type || '(' || character_maximum_length || ')'\n" +
                                "            ELSE data_type\n" +
                                "        END AS column_type,\n" +
                                "        CASE\n" +
                                "            WHEN is_nullable = 'NO' THEN 'NOT NULL'\n" +
                                "            ELSE ''\n" +
                                "        END AS nullable,\n" +
                                "        COALESCE(column_default, '') AS column_default\n" +
                                "    FROM \n" +
                                "        information_schema.columns\n" +
                                "    WHERE \n" +
                                "        table_schema = '%s'\n" +
                                "        AND table_name = '%s'\n" +
                                ")\n" +
                                "SELECT \n" +
                                "    'CREATE TABLE ' || '%s' || ' (' || STRING_AGG(\n" +
                                "        column_name || ' ' || column_type || ' ' || nullable || ' ' || column_default, \n" +
                                "        ', '\n" +
                                "    ) || ');'\n" +
                                "FROM \n" +
                                "    column_info;",
                        tableSchema, tableName, tableName
                );
                break;

            case MARIADB:
            case MYSQL:
                query = String.format(
                        "SHOW CREATE TABLE '%s';",
                        tableName
                );
                break;

            case MSSQLSERVER:
                query = String.format(
                        "EXEC sp_helptext '%s';",
                        tableName
                );
                break;

            case ORACLE21:
                query = String.format(
                        "SELECT \n" +
                                "    DBMS_METADATA.GET_DDL('TABLE', '%s', '%s') \n" +
                                "FROM \n" +
                                "    DUAL;",
                        tableName, tableName
                );
                break;
            case SNOWFLAKE:
                query = String.format(
                        "SELECT \n" +
                                "    GET_DDL('TABLE', '%s');",
                        tableName
                );
                break;
            default:
                throw new IllegalArgumentException("Not a valid JDBC type");
        }

        return jdbcService.executeJdbc(databaseSourceConfig, query);
    }

    public Results getSqlMetaData(DatabaseSourceConfig databaseSourceConfig, String tableName) throws Exception {
        String query = null;
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                query = String.format(
                        "SELECT \n" +
                                "    c.relname AS table_name,\n" +
                                "    c.reltuples AS row_count,\n" +
                                "    pg_total_relation_size(c.oid) AS size,\n" +
                                "    NULL AS created_at, \n" +
                                "    NULL AS updated_at\n" +
                                "FROM \n" +
                                "    pg_class c\n" +
                                "JOIN \n" +
                                "    pg_namespace n ON n.oid = c.relnamespace\n" +
                                "LEFT JOIN \n" +
                                "    pg_stat_all_tables t ON c.oid = t.relid\n" +
                                "WHERE \n" +
                                "    c.relkind = 'r'\n" +
                                "    AND c.relname = '%s';",
                        tableName
                );
                break;

            case MARIADB:
            case MYSQL:
                query = String.format(
                        "SELECT \n" +
                                "    TABLE_NAME AS table_name,\n" +
                                "    TABLE_ROWS AS row_count,\n" +
                                "    DATA_LENGTH + INDEX_LENGTH AS size,  -- Size in MB\n" +
                                "    NULL AS created_at, \n" +
                                "    NULL AS updated_at\n" +
                                "FROM \n" +
                                "    information_schema.tables \n" +
                                "WHERE \n" +
                                "    TABLE_TYPE = 'BASE TABLE' \n" +
                                "    AND TABLE_SCHEMA = '%s' \n" +
                                "    AND TABLE_NAME = '%s';",
                        databaseSourceConfig.getDatabase(), tableName
                );
                break;

            case MSSQLSERVER:
                query = String.format(
                        "SELECT \n" +
                                "    t.name AS table_name,\n" +
                                "    p.rows AS row_count,\n" +
                                "    SUM(a.total_pages) * 8 AS size,\n" +
                                "    NULL AS created_at,\n" +
                                "    NULL AS updated_at\n" +
                                "FROM \n" +
                                "    sys.tables t\n" +
                                "INNER JOIN \n" +
                                "    sys.indexes i ON t.object_id = i.object_id\n" +
                                "INNER JOIN \n" +
                                "    sys.partitions p ON i.object_id = p.object_id AND i.index_id = p.index_id\n" +
                                "INNER JOIN \n" +
                                "    sys.allocation_units a ON p.partition_id = a.container_id\n" +
                                "WHERE \n" +
                                "    i.index_id <= 1  -- 0 for heap tables, 1 for clustered indexes\n" +
                                "    AND t.name = '%s'\n" +
                                "GROUP BY \n" +
                                "    t.name, p.rows;",
                        tableName
                );
                break;

            case ORACLE21:
                query = String.format(
                        "SELECT \n" +
                                "    table_name,\n" +
                                "    num_rows AS row_count,\n" +
                                "    blocks * 8192 AS size,\n" +
                                "    NULL AS created_at,\n" +
                                "    NULL AS updated_at\n" +
                                "FROM \n" +
                                "    user_tables\n" +
                                "WHERE \n" +
                                "    table_name = '%s'",
                        tableName
                );
                break;
            case SNOWFLAKE:
                query = String.format(
                        "SELECT TABLE_NAME as TABLE_NAME, ROW_COUNT as ROW_COUNT, BYTES as SIZE, CREATED as CREATED_AT, LAST_ALTERED as UPDATED_AT from information_schema.tables where TABLE_TYPE = 'BASE TABLE' AND TABLE_NAME = '%s';",
                        tableName
                );
                break;
            default:
                throw new IllegalArgumentException("Not a valid JDBC type");
        }

        return jdbcService.executeJdbc(databaseSourceConfig, query);
    }

    public String fallbackRowsCount(DatabaseSourceConfig databaseSourceConfig, String tableName, String originalValue) throws Exception {
        try {
            switch (databaseSourceConfig.getDbmsType()) {
                case POSTGRES:
                    return jdbcService.executeJdbc(databaseSourceConfig, "SELECT COUNT(*) FROM " + tableName).getResults().get(0).getData().get(0).get(0);
                default:
                    return originalValue;
            }
        } catch (Exception e) {
            log.error("Failed to count rows in fallback method for {}", tableName, e);
            return originalValue;
        }
    }

    public String getSqlDDLQuery(DatabaseSourceConfig databaseSourceConfig, String tableName) throws Exception {
        return "";
    }

    public ResourceModel getSqlTreeData(DatabaseSourceConfig databaseSourceConfig, ResourceModel sourceResourceModel, ResourceType resourceType) throws Exception {
        Results results = getSourceTables(databaseSourceConfig);
        Set<ResourceModel> childrenResourceModel = new HashSet<>();
        HashMap<String, ResourceModel> resourceMap = new HashMap<>();

        for (List<String> row : results.getResults().get(0).getData()) {
            ResourceModel schemaResourceModel = null;
            Set<ResourceModel> schemaTables = new TreeSet<>(Comparator.comparing(ResourceModel::getName));

            if (resourceMap.get(row.get(0)) == null) {
                schemaResourceModel = new ResourceModel();
                schemaResourceModel.setId(UUID.randomUUID());
                schemaResourceModel.setName(row.get(0));
                schemaResourceModel.setType(resourceType);
                schemaResourceModel.setSubType(ResourceSubtype.SCHEMAFOLDER);
                childrenResourceModel.add(schemaResourceModel);
            } else {
                schemaResourceModel = resourceMap.get(row.get(0));
                schemaTables = schemaResourceModel.getChildren();
            }

            ResourceModel tableModel = new ResourceModel();
            tableModel.setId(UUID.randomUUID());
            tableModel.setName(row.get(1));
            tableModel.setType(resourceType);
            tableModel.setSubType(ResourceSubtype.TABLE);
            Set<ResourceModel> tableColumns = new TreeSet<>(Comparator.comparing(ResourceModel::getName));

            tableModel.setChildren(tableColumns);
            schemaTables.add(tableModel);
            schemaResourceModel.setChildren(schemaTables);

            resourceMap.put(row.get(0), schemaResourceModel);
        }

        sourceResourceModel.setType(resourceType);
        sourceResourceModel.setChildren(childrenResourceModel);
        return sourceResourceModel;
    }

    public ResourceModel getSourceSqlTreeData(DatabaseSourceConfig databaseSourceConfig, ResourceType resourceType, UUID sourceId, UUID userId) throws Exception {
        ResourceModel sourceResourceModel = resourceService.getResourceModelWithChildren(sourceId, userId);
        return getSqlTreeData(databaseSourceConfig, sourceResourceModel, resourceType);
    }

    public ResourceModel buildFileSystemTree(File file, ResourceType resourceType) throws Exception {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setId(UUID.randomUUID());
        resourceModel.setName(file.getName());
        resourceModel.setType(resourceType);
        resourceModel.setSubType(file.isDirectory() ? ResourceSubtype.FOLDER : ResourceSubtype.FILE);
        Set<ResourceModel> children = new HashSet<>();

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    children.add(buildFileSystemTree(child, resourceType));
                }
            }
        }

        resourceModel.setChildren(children);

        return resourceModel;
    }

    public ResourceModel getFileSystemTreeData(FolderSourceConfig folderSourceConfig, UUID sourceId, UUID userId) throws Exception {
        String path = folderSourceConfig.getPath();
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            return buildFileSystemTree(folder, ResourceType.FILESYSTEMSOURCE);
        }

        return null;
    }

    public ResourceModel getSourceContentMetaData(UUID sourceId, UUID userId) throws Exception {
        Source source = getSource(sourceId);
        // Get Tables
        if (Objects.equals(source.getType(), "FOLDER")) {
            Optional<FolderSourceConfig> folderSourceConfig = getFolderSourceConfig(sourceId);
            if (folderSourceConfig.isPresent()) {
                return getFileSystemTreeData(folderSourceConfig.get(), sourceId, userId);
            }
        } else if (Objects.equals(source.getType(), "rest")) {
            Optional<RestAPISourceConfig> restAPISourceConfig = getRestAPISourceConfig(sourceId);
            return null;
        } else if (Objects.equals(source.getType(), "jdbc")) {
            DatabaseSourceConfig databaseSourceConfig = getSourceDatabaseSourceConfig(sourceId);
            ResourceType resourceType = getResourceType(databaseSourceConfig.getDbmsType());
            return getSourceSqlTreeData(databaseSourceConfig, resourceType, sourceId, userId);
        }
        return null;

    }

    public Results getSourceTables(DatabaseSourceConfig databaseSourceConfig) throws Exception {
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                String postgresQuery = "SELECT DISTINCT table_schema, table_name, table_type\n" +
                        "FROM information_schema.tables;";
                return jdbcService.executeJdbc(databaseSourceConfig, postgresQuery);
            case MARIADB:
                String mariaQuery = "SELECT DISTINCT table_schema, table_name, table_type\n" +
                        "FROM information_schema.tables;";
                return jdbcService.executeJdbc(databaseSourceConfig, mariaQuery);
            case MYSQL:
                String mysqlQuery = "SELECT DISTINCT table_schema, table_name, table_type\n" +
                        "FROM information_schema.tables;";
                return jdbcService.executeJdbc(databaseSourceConfig, mysqlQuery);
            case MSSQLSERVER:
                String mssqlServerQuery = "SELECT DISTINCT table_schema, table_name, table_type\n" +
                        "FROM information_schema.tables;";
                return jdbcService.executeJdbc(databaseSourceConfig, mssqlServerQuery);
            case ORACLE21:
                String oracle21Query = "SELECT DISTINCT OWNER AS table_schema, TABLE_NAME AS table_name, 'TABLE' AS table_type\n" +
                        "FROM ALL_TABLES UNION\n" +
                        "SELECT DISTINCT OWNER AS table_schema, VIEW_NAME AS table_name, 'VIEW' AS table_type\n" +
                        "FROM ALL_VIEWS ORDER BY table_schema, table_name;";
                return jdbcService.executeJdbc(databaseSourceConfig, oracle21Query);
            case SNOWFLAKE:
                String snowflakeQuery = "SELECT table_schema, table_name, table_type\n" +
                        "FROM " + databaseSourceConfig.getDatabase() + ".information_schema.tables;";
                return jdbcService.executeJdbc(databaseSourceConfig, snowflakeQuery);
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    public Results getSourceTableSchema(DatabaseSourceConfig databaseSourceConfig, String tableName) throws Exception {
        switch (databaseSourceConfig.getDbmsType()) {
            case POSTGRES:
                String postgresQuery = "SELECT column_name, data_type, character_maximum_length, " +
                        "numeric_precision, numeric_scale, is_nullable " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = '" + tableName + "';";

                return jdbcService.executeJdbc(databaseSourceConfig, postgresQuery);

            case MARIADB:
                String mariaQuery = "SELECT column_name, data_type, character_maximum_length, " +
                        "numeric_precision, numeric_scale, is_nullable " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = '" + tableName + "';";

                return jdbcService.executeJdbc(databaseSourceConfig, mariaQuery);
            case MYSQL:
                String mysqlQuery = "SELECT column_name, data_type, character_maximum_length, " +
                        "numeric_precision, numeric_scale, is_nullable " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = '" + tableName + "';";

                return jdbcService.executeJdbc(databaseSourceConfig, mysqlQuery);

            case MSSQLSERVER:
                String msSqlServer = "SELECT column_name, data_type, character_maximum_length, " +
                        "numeric_precision, numeric_scale, is_nullable " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = '" + tableName + "';";

                return jdbcService.executeJdbc(databaseSourceConfig, msSqlServer);

            case ORACLE21:
                String oracle21Query = "SELECT \n" +
                        "    COLUMN_NAME,\n" +
                        "    DATA_TYPE,\n" +
                        "    DATA_LENGTH AS CHARACTER_MAXIMUM_LENGTH,\n" +
                        "    DATA_PRECISION AS NUMERIC_PRECISION,\n" +
                        "    DATA_SCALE AS NUMERIC_SCALE,\n" +
                        "    NULLABLE AS IS_NULLABLE\n" +
                        "FROM \n" +
                        "    ALL_TAB_COLUMNS\n" +
                        "WHERE \n" +
                        "    TABLE_NAME = '" + tableName + "';";
                return jdbcService.executeJdbc(databaseSourceConfig, oracle21Query);
            case SNOWFLAKE:
                String snowflakeQuery = "SELECT column_name, data_type, character_maximum_length, " +
                        "numeric_precision, numeric_scale, is_nullable " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = '" + tableName + "';";
                return jdbcService.executeJdbc(databaseSourceConfig, snowflakeQuery);

            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }
}
