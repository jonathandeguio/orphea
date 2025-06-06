ALTER TABLE deployments
    ADD capture_last_application_log_timestamp BIGINT;

ALTER TABLE deployments
    ADD frontend_last_application_log_timestamp BIGINT;