package io.movetodata.connect.library.enums;

public enum SourceTypeEnum {
    JDBC("jdbc"),
    POSTGRES("postgres"),
    MYSQL("mysql"),
    ORACLE21("oracle21"),
    MSSQLSERVER("mssql server"),
    MARIADB("mariadb"),
    SNOWFLAKE("snowflake"),
    ODBC("odbc"),
    CLICKHOUSE("clickhouse"),
    DATABRICKS("databricks"),
    MONGODB("mongodb"),
    // For internal usage only
    SPARKSQL("sparkSql"),
    NONE("NONE"),
    FILESYSTEM("file system"),
    SHAREPOINT("file system");

    private final String displayName;

    SourceTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDatasetMappingEnums() {
        return displayName;
    }
}
