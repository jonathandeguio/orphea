CREATE TABLE sharepoint_source_config
(
    id            UUID NOT NULL,
    tenant_id     VARCHAR(255),
    client_id     VARCHAR(255),
    client_secret VARCHAR(255),
    url           VARCHAR(255),
    site_id       VARCHAR(255),
    drive_id      VARCHAR(255),
    token         TEXT,
    CONSTRAINT pk_sharepoint_source_config PRIMARY KEY (id)
);