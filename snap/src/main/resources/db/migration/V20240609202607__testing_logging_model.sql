ALTER TABLE qrtz_cron_triggers
    DROP CONSTRAINT fk_qrtz_cron_triggers_qrtz_triggers;

ALTER TABLE qrtz_simple_triggers
    DROP CONSTRAINT fk_qrtz_simple_triggers_qrtz_triggers;

ALTER TABLE qrtz_simprop_triggers
    DROP CONSTRAINT fk_qrtz_simprop_triggers_qrtz_triggers;

ALTER TABLE qrtz_triggers
    DROP CONSTRAINT fk_qrtz_triggers_qrtz_job_details;

CREATE TABLE deployment_return_model
(
    id            UUID NOT NULL,
    deployment_id UUID,
    access_token  VARCHAR(255),
    CONSTRAINT pk_deploymentreturnmodel PRIMARY KEY (id)
);

CREATE TABLE logging_model
(
    id          UUID NOT NULL,
    metric_type VARCHAR(255),
    time_stamp  VARCHAR(255),
    tag_id      UUID,
    CONSTRAINT pk_logging_model PRIMARY KEY (id)
);

CREATE TABLE logging_tags
(
    id          UUID NOT NULL,
    trace_id    VARCHAR(255),
    object_name VARCHAR(255),
    level       VARCHAR(255),
    message     VARCHAR(255),
    CONSTRAINT pk_logging_tags PRIMARY KEY (id)
);

CREATE TABLE version
(
    id                  UUID NOT NULL,
    name                VARCHAR(255),
    resource_version_id VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    created_by          UUID,
    updated_by          UUID,
    CONSTRAINT pk_version PRIMARY KEY (id)
);

ALTER TABLE logging_model
    ADD CONSTRAINT FK_LOGGING_MODEL_ON_TAG FOREIGN KEY (tag_id) REFERENCES logging_tags (id);

DROP TABLE build_trigger_event CASCADE;

DROP TABLE kitab_resource CASCADE;

DROP TABLE qrtz_blob_triggers CASCADE;

DROP TABLE qrtz_calendars CASCADE;

DROP TABLE qrtz_cron_triggers CASCADE;

DROP TABLE qrtz_fired_triggers CASCADE;

DROP TABLE qrtz_job_details CASCADE;

DROP TABLE qrtz_locks CASCADE;

DROP TABLE qrtz_paused_trigger_grps CASCADE;

DROP TABLE qrtz_scheduler_state CASCADE;

DROP TABLE qrtz_simple_triggers CASCADE;

DROP TABLE qrtz_simprop_triggers CASCADE;

DROP TABLE qrtz_triggers CASCADE;

ALTER TABLE build_artifact
    DROP COLUMN status;

ALTER TABLE build_trigger
    DROP COLUMN build_status;

ALTER TABLE build_trigger
    ADD build_status SMALLINT;

ALTER TABLE license
    ALTER COLUMN display_blocked_features SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN expires_on SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN maximum_builds_per_day SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN maximum_charts SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN maximum_dashboards SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN maximum_datasets SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN maximum_repositories SET NOT NULL;

ALTER TABLE license
    ALTER COLUMN maximum_users SET NOT NULL;