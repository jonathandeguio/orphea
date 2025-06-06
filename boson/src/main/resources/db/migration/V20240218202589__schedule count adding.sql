ALTER TABLE scheduler_job_info
    ADD failure_execution_count BIGINT;

ALTER TABLE scheduler_job_info
    ADD success_execution_count BIGINT;