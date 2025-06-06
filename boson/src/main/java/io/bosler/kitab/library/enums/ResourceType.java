package io.bosler.kitab.library.enums;

public enum ResourceType {
    FILE("File"),
    FOLDER("Folder"),
    CHART("Chart"),
    PROJECT("Project"),
    REPOSITORY("Repository"),
    DASHBOARD("Dashboard"),
    CONNECT("Connect"),
    AGENT("Agent"),
    SOURCE("Source"),
    WEBHOOK("Webhook"),

    POSTGRESSOURCE("Postgres source"),
    MARIASOURCE("Maria db source"),
    MYSQLSERVERSOURCE("MySQL server source"),
    ORACLE21SOURCE("Oracle DB source"),
    MYSQLSOURCE("Mysql source"),
    SNOWFLAKESOURCE("Snowflake source"),
    FILESYSTEMSOURCE("Filesystem source"),

    LINK("Link"),
    DATASET("Dataset");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
