package io.bosler.connect.library.services;

import io.bosler.connect.library.enums.SourceTypeEnum;
import io.bosler.sharedutils.Exceptions.DatabaseOperationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception var2) {
                Exception e = var2;
            }

        }
    }

    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException var2) {
                SQLException e = var2;
            }

        }
    }

    public static boolean isValidDQLQuery(String query, SourceTypeEnum source) {
        String trimmedQuery = query.trim().toUpperCase();

        switch (source) {
            case MYSQL:
            case MARIADB:
                return isMySQLSpecificDQLQuery(trimmedQuery);
            case POSTGRES:
                return isPostgreSQLSpecificDQLQuery(trimmedQuery);
            case ORACLE21:
                return isOracleSpecificDQLQuery(trimmedQuery);
            case MSSQLSERVER:
                return isSQLServerSpecificDQLQuery(trimmedQuery);
            case SNOWFLAKE:
                return isSnowflakeSpecificDQLQuery(trimmedQuery);
            case SPARKSQL:
                return isSparkSQLSpecificDQLQuery(trimmedQuery);
            default:
                throw new DatabaseOperationException("Not a valid JDBC type");
        }
    }

    private static boolean isSparkSQLSpecificDQLQuery(String query) {
        return query.startsWith("SELECT ") ||
                query.startsWith("WITH ") ||
                query.startsWith("SHOW ") ||
                query.startsWith("DESCRIBE ") ||
                query.startsWith("EXPLAIN ") ||
                query.startsWith("CACHE ") ||
                query.startsWith("UNCACHE ") ||
                query.startsWith("MSCK ") ||
                query.startsWith("REFRESH ");
    }

    private static boolean isMySQLSpecificDQLQuery(String query) {
        return query.startsWith("SELECT ") ||
                query.startsWith("WITH ") ||
                query.startsWith("SHOW ") ||
                query.startsWith("DESCRIBE ") ||
                query.startsWith("EXPLAIN ");
    }

    private static boolean isPostgreSQLSpecificDQLQuery(String query) {
        return query.startsWith("SELECT ") ||
                query.startsWith("WITH ") ||
                query.startsWith("EXPLAIN ");
    }

    private static boolean isOracleSpecificDQLQuery(String query) {
        return query.startsWith("SELECT ") ||
                query.startsWith("WITH ") ||
                query.startsWith("EXPLAIN PLAN FOR ");
    }

    private static boolean isSQLServerSpecificDQLQuery(String query) {
        return query.startsWith("SELECT ") ||
                query.startsWith("WITH ") ||
                query.startsWith("EXPLAIN ");
    }

    private static boolean isSnowflakeSpecificDQLQuery(String query) {
        return query.startsWith("SELECT ") ||
                query.startsWith("WITH ") ||
                query.startsWith("SHOW ") ||
                query.startsWith("DESCRIBE ") ||
                query.startsWith("EXPLAIN ");
    }
}
