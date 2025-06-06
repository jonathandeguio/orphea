ALTER TABLE deployments
    ADD last_cpu_metric_log_timestamp TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE deployments
    ADD last_disk_metric_log_timestamp TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE deployments
    ADD last_memory_metric_log_timestamp TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE deployments
    ADD last_swap_metric_log_timestamp TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE deployments
    DROP COLUMN last_metric_log_timestamp;