CREATE TABLE IF NOT EXISTS scheduler_job_info_logs
(
    scheduler_job_info_job_id UUID NOT NULL,
    logs_id                   UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS scheduler_job_logs_execution_logs_details
(
    owner_id              UUID NOT NULL,
    execution_logs_detail VARCHAR(255)
);

-- ALTER TABLE scheduler_job_info_logs
--     ADD CONSTRAINT uc_scheduler_job_info_logs_logs UNIQUE (logs_id);

-- ALTER TABLE scheduler_job_logs_execution_logs_details
--     ADD CONSTRAINT fk_schedulerjoblogsexecutionlogsdetails_on_schedulerjoblogs FOREIGN KEY (owner_id) REFERENCES scheduler_job_logs (id);

-- ALTER TABLE scheduler_job_logs
--     DROP CONSTRAINT fk_schjobinflog_on_scheduler_job_info;

-- ALTER TABLE scheduler_job_info_logs
--     ADD CONSTRAINT fk_schjobinflog_on_scheduler_job_info FOREIGN KEY (scheduler_job_info_job_id) REFERENCES scheduler_job_info (job_id);

-- ALTER TABLE scheduler_job_info_logs
--     ADD CONSTRAINT fk_schjobinflog_on_scheduler_job_logs FOREIGN KEY (logs_id) REFERENCES scheduler_job_logs (id);


-- ALTER TABLE scheduler_job_logs
--     DROP COLUMN execution_logs;

-- ALTER TABLE scheduler_job_logs
--     DROP COLUMN scheduler_job_info_job_id;

-- ALTER TABLE schedule_trigger
--     DROP COLUMN job_class;

-- ALTER TABLE schedule_trigger
--     DROP COLUMN trigger_type;

ALTER TABLE scheduler_job_info
    DROP COLUMN last_execution;

ALTER TABLE scheduler_job_info
    ADD last_execution TIMESTAMP WITHOUT TIME ZONE;