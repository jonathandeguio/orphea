CREATE TABLE dataset_meta_data
(
    id                        UUID    NOT NULL,
    is_default_branch_present BOOLEAN NOT NULL,
    build_id                  UUID,
    transaction_id            UUID,
    build_trigger             SMALLINT,
    CONSTRAINT pk_dataset_meta_data PRIMARY KEY (id)
);