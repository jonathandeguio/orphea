CREATE TABLE datamart_config_model
(
    id          UUID    NOT NULL,
    name        VARCHAR(255),
    description TEXT,
    is_enabled  BOOLEAN NOT NULL,
    projects    UUID[],
    server      VARCHAR(255),
    port        INTEGER,
    database    VARCHAR(255),
    username    VARCHAR(255),
    password    VARCHAR(255),
    limit_rows  BIGINT DEFAULT -1,
    limit_size  BIGINT DEFAULT -1,
    updated_by  UUID,
    created_by  UUID,
    updated_at  TIMESTAMP,
    created_at  TIMESTAMP
);