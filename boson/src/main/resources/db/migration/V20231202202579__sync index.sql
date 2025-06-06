CREATE TABLE sync_index
(
    id                    UUID NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE,
    created_by            UUID,
    updated_by            UUID,
    updated_at            TIMESTAMP WITHOUT TIME ZONE,
    sync_specification_id UUID,
    CONSTRAINT pk_sync_index PRIMARY KEY (id)
);

CREATE TABLE sync_index_columns
(
    owner_id UUID NOT NULL,
    "column" VARCHAR(255)
);

ALTER TABLE sync_index
    ADD CONSTRAINT FK_SYNC_INDEX_ON_SYNC_SPECIFICATION FOREIGN KEY (sync_specification_id) REFERENCES sync_specification (id);

ALTER TABLE sync_index_columns
    ADD CONSTRAINT fk_sync_index_columns_on_sync_index FOREIGN KEY (owner_id) REFERENCES sync_index (id);