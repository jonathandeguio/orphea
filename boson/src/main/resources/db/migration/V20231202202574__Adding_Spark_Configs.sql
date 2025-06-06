CREATE TABLE platform_config_spark
(
    config         VARCHAR(255) NOT NULL,
    master         VARCHAR(255),
    pg_sync        VARCHAR(255),
    dataset        VARCHAR(255),
    column_stats   VARCHAR(255),
    sql_build      VARCHAR(255),
    python_build   VARCHAR(255),
    sql_preview    VARCHAR(255),
    python_preview VARCHAR(255),
    CONSTRAINT pk_platform_config_spark PRIMARY KEY (config)
);