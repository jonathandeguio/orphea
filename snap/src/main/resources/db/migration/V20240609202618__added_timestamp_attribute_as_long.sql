ALTER TABLE deployments
    DROP COLUMN last_access_log_timestamp;

ALTER TABLE deployments
    DROP COLUMN last_application_log_timestamp;

ALTER TABLE deployments
    DROP COLUMN last_cpu_metric_log_timestamp;

ALTER TABLE deployments
    DROP COLUMN last_disk_metric_log_timestamp;

ALTER TABLE deployments
    DROP COLUMN last_memory_metric_log_timestamp;

ALTER TABLE deployments
    DROP COLUMN last_swap_metric_log_timestamp;

ALTER TABLE deployments
    ADD last_access_log_timestamp BIGINT;

ALTER TABLE deployments
    ADD last_application_log_timestamp BIGINT;

ALTER TABLE deployments
    ADD last_cpu_metric_log_timestamp BIGINT;

ALTER TABLE deployments
    ADD last_disk_metric_log_timestamp BIGINT;

ALTER TABLE deployments
    ADD last_memory_metric_log_timestamp BIGINT;

ALTER TABLE deployments
    ADD last_swap_metric_log_timestamp BIGINT;