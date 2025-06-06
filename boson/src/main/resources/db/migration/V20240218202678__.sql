CREATE TABLE data_mart_model_projects
(
    data_mart_model_id UUID NOT NULL,
    projects           UUID
);

ALTER TABLE platform_config_datamart
    DROP COLUMN host;

ALTER TABLE platform_config_datamart
    DROP COLUMN port;

ALTER TABLE platform_config_datamart
    DROP COLUMN username;

ALTER TABLE platform_config_datamart
    DROP COLUMN password;

ALTER TABLE platform_config_datamart
    DROP COLUMN database_driver;
ALTER TABLE platform_config_datamart
    DROP COLUMN ignore_datamart_users;
ALTER TABLE platform_config_datamart
    DROP COLUMN limit_rows;
ALTER TABLE platform_config_datamart
    DROP COLUMN limit_size;
ALTER TABLE platform_config_datamart
    DROP COLUMN database;
ALTER TABLE platform_config_datamart
    DROP COLUMN data_source_config_id;

CREATE TABLE platform_config_datamart_data_mart_models
(
    data_mart_config_model_config VARCHAR(255) NOT NULL,
    data_mart_models_id           UUID         NOT NULL
);

ALTER TABLE database_source_config
    ADD limit_rows BIGINT;

ALTER TABLE database_source_config
    ADD limit_size BIGINT;

ALTER TABLE platform_config
    DROP COLUMN IF EXISTS datamart_enabled;

ALTER TABLE platform_config
    ADD data_mart_enabled BOOLEAN;

ALTER TABLE sync_specification
    ADD data_mart_id UUID;

ALTER TABLE sync_specification
    ADD is_data_mart_sync_spec BOOLEAN;

ALTER TABLE sync_specification
    ALTER COLUMN is_data_mart_sync_spec SET DEFAULT false;

UPDATE sync_specification
SET is_data_mart_sync_spec = false;

