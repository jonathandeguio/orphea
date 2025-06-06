CREATE TABLE platform_config_backingfs
(
    config         VARCHAR(255) NOT NULL,
    fs_type        VARCHAR(255),
    gs_bucket      VARCHAR(255),
    gs_credentials VARCHAR(255),
    local_fs       VARCHAR(255),
    hdfs           VARCHAR(255),
    s3bucket       VARCHAR(255),
    s3access_key   VARCHAR(255),
    s3secret_key   VARCHAR(255),
    CONSTRAINT pk_platform_config_backingfs PRIMARY KEY (config)
);