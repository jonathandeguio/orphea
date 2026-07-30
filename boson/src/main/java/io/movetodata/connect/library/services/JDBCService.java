package io.movetodata.connect.library.services;

import io.movetodata.connect.library.enums.SourceAuthTypeEnum;
import io.movetodata.connect.library.enums.SourceTypeEnum;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.connect.library.models.Results;
import io.movetodata.sharedutils.Exceptions.DatabaseOperationException;
import io.movetodata.sharedutils.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.security.PrivateKey;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static io.movetodata.sharedutils.Utils.isBase64;

@Slf4j
@Component
@AllArgsConstructor
public class JDBCService {

    private static String removeComments(String sql) {
        // Remove single-line comments
        sql = sql.replaceAll("--.*", "");

        // Remove multi-line comments
        sql = sql.replaceAll("/\\*(?s).*?\\*/", "");

        return sql;
    }

    public static String JDBCUrl(DatabaseSourceConfig databaseSourceConfig) {
        String jdbcUrl;
        String server = databaseSourceConfig.getServer();
        Integer port = databaseSourceConfig.getPort();
        String databaseName = databaseSourceConfig.getDatabase();

        switch (databaseSourceConfig.getDbmsType()) {
            case MSSQLSERVER:
                jdbcUrl = "jdbc:sqlserver://" + server + ":" + port + ";" + "databaseName=" + databaseName + ";" + "encrypt=false;trustServerCertificate=false;loginTimeout=30;";
                break;
            case POSTGRES:
                jdbcUrl = "jdbc:postgresql://" + server + ":" + port + "/" + databaseName;
                break;
            case ORACLE21:
                jdbcUrl = "jdbc:oracle:thin:@" + server + ":" + port + ":" + databaseName + "?oracle.jdbc.timezoneAsRegion=false";
                break;
            case MYSQL:
                jdbcUrl = "jdbc:mysql://" + server + ":" + port + "/" + databaseName;
                break;
            case MARIADB:
                jdbcUrl = "jdbc:mariadb://" + server + ":" + port + "/" + databaseName;
                break;
            case SNOWFLAKE:
                jdbcUrl = "jdbc:snowflake://" + server + ".snowflakecomputing.com/";
                jdbcUrl += "?db=" + databaseName;
                if (databaseSourceConfig.getSchema() != null && !databaseSourceConfig.getSchema().isEmpty()) {
                    jdbcUrl += "&schema=" + databaseSourceConfig.getSchema();
                }
                if (databaseSourceConfig.getWarehouse() != null && !databaseSourceConfig.getWarehouse().isEmpty()) {
                    jdbcUrl += "&warehouse=" + databaseSourceConfig.getWarehouse();
                }
                if (databaseSourceConfig.getUserRole() != null && !databaseSourceConfig.getUserRole().isEmpty()) {
                    jdbcUrl += "&role=" + databaseSourceConfig.getUserRole();
                }
                break;
            case CLICKHOUSE:
                jdbcUrl = "jdbc:clickhouse://" + server + ":" + port + "/" + databaseName;
                break;
            case DATABRICKS:
                // Authentication uses UID=token + PWD=<personal-access-token> (AuthMech=3).
                // The PAT is stored in the password field and passed via JDBC Properties.
                // The httpPath is stored in the schema field.
                String httpPath = databaseSourceConfig.getSchema() != null ? databaseSourceConfig.getSchema() : "";
                jdbcUrl = "jdbc:databricks://" + server + ":443/default"
                        + ";transportMode=http;ssl=1"
                        + ";httpPath=" + httpPath
                        + ";AuthMech=3";
                break;
            case ODBC:
                // Two connection modes:
                //   DSN mode        : server field holds the ODBC DSN name → jdbc:odbc:<dsnName>
                //   Connection String mode : database field holds the full connection string
                //                     e.g. "Driver={FreeTDS};Server=myserver;Port=1433;Database=mydb"
                //                     → jdbc:odbc:<connectionString>
                // TODO(Docker): the container image must include unixODBC and the relevant ODBC
                //               drivers (FreeTDS for Sybase/MSSQL legacy, libmdbtools for MS Access, etc.).
                //               Configure DSN sources in /etc/odbc.ini and drivers in /etc/odbcinst.ini.
                if (databaseSourceConfig.getDatabase() != null && !databaseSourceConfig.getDatabase().isEmpty()) {
                    // Connection String mode: pass the full connection string as the ODBC DSN portion
                    jdbcUrl = "jdbc:odbc:" + databaseSourceConfig.getDatabase();
                } else {
                    // DSN mode: use the declared Data Source Name
                    jdbcUrl = "jdbc:odbc:" + server;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported JDBC type: " + databaseSourceConfig.getDbmsType());
        }

        return jdbcUrl;
    }

    public String getDriver(SourceTypeEnum jdbcType) {
        switch (jdbcType) {
            case POSTGRES:
                return "org.postgresql.Driver";
            case MYSQL:
                return "com.mysql.cj.jdbc.Driver";
            case MARIADB:
                return "org.mariadb.jdbc.Driver";
            case ORACLE21:
                return "oracle.jdbc.driver.OracleDriver";
            case MSSQLSERVER:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case SNOWFLAKE:
                return "net.snowflake.client.jdbc.SnowflakeDriver";
            case CLICKHOUSE:
                return "com.clickhouse.jdbc.ClickHouseDriver";
            case DATABRICKS:
                return "com.databricks.client.jdbc.Driver";
            case ODBC:
                // TODO(build.gradle): add a JDBC-ODBC bridge dependency, e.g.:
                //   implementation 'com.hynnet:odbc-bridge:1.0.3'   (JNI, requires unixODBC)
                // or the Easysoft/OpenLink commercial bridge for production Enterprise deployments.
                // sun.jdbc.odbc.JdbcOdbcDriver was removed in Java 8 — a third-party bridge is mandatory.
                try {
                    Class.forName("com.hynnet.odbc.Driver");
                    return "com.hynnet.odbc.Driver";
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(
                        "ODBC bridge driver not found on the classpath. " +
                        "Add a JDBC-ODBC bridge jar to build.gradle (e.g. com.hynnet:odbc-bridge) " +
                        "and ensure unixODBC with the required drivers is installed in the Docker image. " +
                        "Refer to the ODBC Bridge architecture spec (§5).",
                        e
                    );
                }
            default:
                throw new IllegalArgumentException("Unsupported JDBC type: " + jdbcType);
        }
    }

    private String removeTrailingSemicolon(String query) {
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

    private String addLimitClause(String query, SourceTypeEnum jdbcType, int limit) {
        if (limit > 0) {
            switch (jdbcType) {
                case POSTGRES:
                case MYSQL:
                case SNOWFLAKE:
                case MARIADB:
                case CLICKHOUSE:
                case DATABRICKS:
                    query = query + " LIMIT " + limit;
                    break;
                case ORACLE21:
                    // Wrap the query to apply ROWNUM in the outer query to avoid interfering with subqueries
                    query = "SELECT * FROM (" + query + ") WHERE ROWNUM <= " + limit;
                    break;
                case MSSQLSERVER:
                    query = query.replaceFirst("SELECT | select", String.format("SELECT TOP (%d)", limit));
                    break;
                case ODBC:
                    // Use ANSI SQL LIMIT as a best-effort default for ODBC sources.
                    // Note: some legacy backends (IBM AS/400, Sybase ASE older versions) may not support LIMIT.
                    // In those cases the query will fail gracefully and the user should add their own LIMIT clause.
                    query = query + " LIMIT " + limit;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported JDBC type: " + jdbcType);
            }
        }
        return query;
    }

    private boolean hasLimitClause(String query) {
        return query.toLowerCase().contains("limit") || query.toLowerCase().contains("rownum") || query.toLowerCase().startsWith("select top");
    }

    public String processQuery(String query, SourceTypeEnum jdbcType, int limit) {
        if (Utils.isBase64(query)) {
            query = Utils.decodeBase64(query);
            query = removeComments(query);
            query = Utils.removeLineBreaks(query);
            query = removeTrailingSemicolon(query);

            if (!hasLimitClause(query) && limit > 0 && JdbcUtils.isValidDQLQuery(query, jdbcType)) {
                query = addLimitClause(query, jdbcType, limit);
            }
        }
        return query;
    }

    public Connection getJdbcConnection(DatabaseSourceConfig databaseSourceConfig) throws Exception {
        Properties properties = new Properties();
        // Databricks JDBC with AuthMech=3 requires the literal username "token" regardless of what the user typed.
        if (SourceTypeEnum.DATABRICKS.equals(databaseSourceConfig.getDbmsType())) {
            properties.put("user", "token");
        } else {
            properties.put("user", databaseSourceConfig.getUsername());
        }

        if (databaseSourceConfig.getAuthType() != null && databaseSourceConfig.getAuthType().equals(SourceAuthTypeEnum.KEYPAIR)) {
            String privateKeyPath = databaseSourceConfig.getPrivateKey();
            String privateKeyPassphrase = databaseSourceConfig.getPrivateKeyPassPhrase();

            PrivateKey privateKey = loadPrivateKey(privateKeyPath, privateKeyPassphrase);
            properties.put("privateKey", privateKey);
        } else {
            String password = databaseSourceConfig.getPassword();
            properties.put("password", isBase64(password) ? Utils.decodeBase64(password) : password);
        }

        return DriverManager.getConnection(JDBCService.JDBCUrl(databaseSourceConfig), properties);
    }

    private PrivateKey loadPrivateKey(String privateKeyString, String privateKeyPassphrase) throws Exception {
        PrivateKeyInfo privateKeyInfo = null;
        Security.addProvider(new BouncyCastleProvider());
        // Read an object from the private key file.
//        PEMParser pemParser = new PEMParser(new FileReader(Paths.get("/home/fa065107/Downloads/idea-IU-232.10227.8/bin/private_key.pem").toFile()));
        PEMParser pemParser = new PEMParser(new StringReader(privateKeyString));
        Object pemObject = pemParser.readObject();
        if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
            // Handle the case where the private key is encrypted.
            PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) pemObject;
            InputDecryptorProvider pkcs8Prov = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(privateKeyPassphrase.toCharArray());
            privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(pkcs8Prov);
        } else if (pemObject instanceof PrivateKeyInfo) {
            // Handle the case where the private key is unencrypted.
            privateKeyInfo = (PrivateKeyInfo) pemObject;
        }
        pemParser.close();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
        return converter.getPrivateKey(privateKeyInfo);

    }


    public Results executeJdbc(DatabaseSourceConfig databaseSourceConfig, String query) throws Exception {
        return executeJdbc(databaseSourceConfig, query, null);
    }

    public Results executeJdbc(DatabaseSourceConfig databaseSourceConfig, String query, UUID buildId) throws Exception {
        Connection connection = getJdbcConnection(databaseSourceConfig);
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(connection);
            return jdbcTemplate.executeStatement(query);
        } catch (Exception e) {
            throw new DatabaseOperationException(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
