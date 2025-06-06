ALTER TABLE scheduler_job_info
    DROP COLUMN job_status;

ALTER TABLE scheduler_job_info
    DROP COLUMN last_job_execution_status;

ALTER TABLE scheduler_job_info
    DROP COLUMN resource_type;

ALTER TABLE scheduler_job_info
    DROP COLUMN trigger_type;

ALTER TABLE scheduler_job_info
    ADD job_status VARCHAR(255);

ALTER TABLE scheduler_job_info
    ADD last_job_execution_status VARCHAR(255);

ALTER TABLE scheduler_job_info
    ADD resource_type VARCHAR(255);

ALTER TABLE scheduler_job_info
    ADD trigger_type VARCHAR(255);