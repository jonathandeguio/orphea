CREATE TABLE rest_api_source_config
(
    id UUID NOT NULL,
    CONSTRAINT pk_rest_api_source_config PRIMARY KEY (id)
);

CREATE TABLE rest_api_source_domain
(
    id            UUID NOT NULL,
    domain        VARCHAR(255),
    auth_type     VARCHAR(255),
    bearer_token  VARCHAR(255),
    api_key_name  VARCHAR(255),
    api_key_value VARCHAR(255),
    port          INTEGER,
    config_id     UUID,
    CONSTRAINT pk_rest_api_source_domain PRIMARY KEY (id)
);