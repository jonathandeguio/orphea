ALTER TABLE scheduler_job_info
    ADD previous_job_status VARCHAR(255);

ALTER TABLE scheduler_job_info
    ADD resource_status VARCHAR(255);