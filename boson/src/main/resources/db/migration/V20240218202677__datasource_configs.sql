CREATE TABLE datamart_configs
(
    dataset_id       UUID NOT NULL,
    table_name       VARCHAR(255),
    sync_id          UUID,
    datamart_enabled BOOLEAN,
    CONSTRAINT pk_datamart_configs PRIMARY KEY (dataset_id)
);

ALTER TABLE sync_index
    ADD datamart_configs_id UUID;

ALTER TABLE sync_index
    ADD CONSTRAINT FK_SYNC_INDEX_ON_DATAMART_CONFIGS FOREIGN KEY (datamart_configs_id) REFERENCES datamart_configs (dataset_id);

ALTER TABLE platform_config_datamart
DROP
COLUMN data_source_config_id;

ALTER TABLE platform_config_datamart
    ADD data_source_config_id UUID;