package io.bosler.build.shyne.Utils;


import io.bosler.connect.library.models.DatabaseSourceConfig;

import java.util.Base64;

public class SynchroUtils {
    public static String decodeBase64(String base64Query) {
        return new String(Base64.getDecoder().decode(base64Query));
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
            default:
                throw new IllegalArgumentException("Unsupported JDBC type: " + databaseSourceConfig.getDbmsType());
        }

        return jdbcUrl;
    }
}
