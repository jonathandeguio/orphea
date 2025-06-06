ALTER TABLE deployments
    ADD last_application_log_timestamp TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE deployments
    ADD last_metric_log_timestamp TIMESTAMP WITHOUT TIME ZONE;