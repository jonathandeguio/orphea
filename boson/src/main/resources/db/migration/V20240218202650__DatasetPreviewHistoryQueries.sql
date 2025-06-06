CREATE TABLE dataset_preview_queries
(
    id             UUID NOT NULL,
    dataset_id     UUID,
    branch         VARCHAR(255),
    transaction_id UUID,
    query          VARCHAR(255),
    user_id        UUID,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    created_by     UUID,
    updated_by     UUID,
    CONSTRAINT pk_dataset_preview_queries PRIMARY KEY (id)
);