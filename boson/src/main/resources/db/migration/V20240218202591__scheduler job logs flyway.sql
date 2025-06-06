ALTER TABLE scheduler_job_logs
    DROP COLUMN job_execution_status;

ALTER TABLE scheduler_job_logs
    ADD job_execution_status VARCHAR(255);