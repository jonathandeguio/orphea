ALTER TABLE scheduler_job_info
ADD COLUMN last_execution DATE,
ADD COLUMN last_job_execution_status SMALLINT;

CREATE TABLE scheduler_job_logs (
  id UUID NOT NULL,
  scheduler_job_info_job_id UUID NOT NULL,
   started_at TIMESTAMP WITHOUT TIME ZONE,
   ended_at TIMESTAMP WITHOUT TIME ZONE,
   job_execution_status SMALLINT,
   execution_logs TEXT[],
   CONSTRAINT pk_scheduler_job_logs PRIMARY KEY (id)
);

ALTER TABLE scheduler_job_logs ADD CONSTRAINT uc_scheduler_job_info_logs_logs UNIQUE (id);

ALTER TABLE scheduler_job_logs ADD CONSTRAINT fk_schjobinflog_on_scheduler_job_info FOREIGN KEY (scheduler_job_info_job_id) REFERENCES scheduler_job_info (job_id);

--ALTER TABLE scheduler_job_logs ADD CONSTRAINT fk_schjobinflog_on_scheduler_job_logs FOREIGN KEY (id) REFERENCES scheduler_job_logs (id);
