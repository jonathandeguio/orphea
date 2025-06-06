CREATE TABLE platform_error
(
    id              UUID NOT NULL,
    name            VARCHAR(255),
    message         VARCHAR(255),
    stack           TEXT,
    component_stack TEXT,
    CONSTRAINT pk_platform_error PRIMARY KEY (id)
);