ALTER TABLE configuration_components
    ADD branch VARCHAR(255);

ALTER TABLE deployments
    ADD branch VARCHAR(255);