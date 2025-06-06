CREATE TABLE sync_specification
(
    id                 UUID    NOT NULL,
    dataset_id         UUID,
    branch             VARCHAR(255),
    source_id          UUID,
    table_name         VARCHAR(255),
    auto_sync_on_build BOOLEAN NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE,
    created_by         UUID,
    updated_by         UUID,
    updated_at         TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_sync_specification PRIMARY KEY (id)
);