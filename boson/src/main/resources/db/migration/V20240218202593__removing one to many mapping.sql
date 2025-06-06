ALTER TABLE scheduler_job_logs
    DROP CONSTRAINT IF EXISTS UK_dataset_mapping_valid_dates;

ALTER TABLE scheduler_job_logs
    DROP COLUMN scheduler_job_info_job_id;

DROP TABLE scheduler_job_info_logs;