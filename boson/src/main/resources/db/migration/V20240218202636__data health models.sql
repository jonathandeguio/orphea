CREATE TABLE data_health_log_model
(
    id              UUID NOT NULL,
    health_check_id UUID,
    dataset_id      UUID,
    branch          VARCHAR(255),
    is_passed       BOOLEAN,
    message         VARCHAR(255),
    is_critical     BOOLEAN,
    started_at      TIMESTAMP WITHOUT TIME ZONE,
    finished_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_data_health_log_model PRIMARY KEY (id)
);

CREATE TABLE data_health_model
(
    id               UUID    NOT NULL,
    rule             VARCHAR(255),
    groups           VARCHAR(255),
    notes            VARCHAR(255),
    issue            BOOLEAN NOT NULL,
    is_enabled       BOOLEAN NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    created_by       UUID,
    updated_by       UUID,
    dataset_id       UUID,
    branch           VARCHAR(255),
    data_health_type VARCHAR(255),
    CONSTRAINT pk_data_health_model PRIMARY KEY (id)
);