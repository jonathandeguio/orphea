CREATE TABLE deployments
(
    id                UUID NOT NULL,
    name              VARCHAR(255),
    location          VARCHAR(255),
    address           VARCHAR(255),
    contact_details   VARCHAR(255),
    email             VARCHAR(255),
    deployment_method VARCHAR(255),
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    created_by        UUID,
    updated_by        UUID,
    CONSTRAINT pk_deployments PRIMARY KEY (id)
);

CREATE TABLE configuration_components
(
    id                   UUID NOT NULL,
    frontend             VARCHAR(255),
    boson                VARCHAR(255),
    parler               VARCHAR(255),
    julia                VARCHAR(255),
    callisto             VARCHAR(255),
    movetodata_docs          VARCHAR(255),
    spark_history_server VARCHAR(255),
    state                VARCHAR(255),
    deployed_at          TIMESTAMP WITHOUT TIME ZONE,
    deployments_id        UUID,
    CONSTRAINT pk_configuration_components PRIMARY KEY (id),
    CONSTRAINT fk_configuration_components_deployment FOREIGN KEY (deployments_id) REFERENCES deployments(id) ON DELETE CASCADE
);

CREATE TABLE license (
    id UUID PRIMARY KEY,
    client VARCHAR(255),
    product VARCHAR(255),
    base_url VARCHAR(255),
    display_blocked_features BOOLEAN,
    maximum_users INT,
    maximum_builds_per_day INT,
    maximum_datasets INT,
    maximum_dashboards INT,
    maximum_charts INT,
    maximum_repositories INT,
    expires_on BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    state VARCHAR(255),
    deployments_id UUID,
    license_key TEXT,
    FOREIGN KEY (deployments_id) REFERENCES deployments(id)
);