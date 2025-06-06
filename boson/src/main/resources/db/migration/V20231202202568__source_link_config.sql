ALTER TABLE connect_links
    DROP COLUMN IF EXISTS source_dataset_config;

ALTER TABLE connect_links
    ADD script TEXT;

ALTER TABLE connect_sources
    DROP COLUMN IF EXISTS source_config;

ALTER TABLE connect_sources
    ADD source_config UUID;

ALTER TABLE connect_sources
    ADD type VARCHAR(255);

CREATE TABLE IF NOT EXISTS database_source_config
(
    id        UUID NOT NULL,
    dbms_type VARCHAR(255),
    username  VARCHAR(255),
    password  VARCHAR(255),
    database  VARCHAR(255),
    server    VARCHAR(255),
    port      INTEGER,
    CONSTRAINT pk_database_source_config PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS folder_source_config
(
    id          UUID NOT NULL,
    path        VARCHAR(255),
    CONSTRAINT pk_folder_source_config PRIMARY KEY (id)
);