ALTER TABLE build_log
    ADD IF NOT EXISTS launched_by VARCHAR(255);

ALTER TABLE build_log
    DROP COLUMN IF EXISTS stage;

ALTER TABLE build_log
    DROP COLUMN IF EXISTS status;

ALTER TABLE build_log
    DROP COLUMN IF EXISTS trigger;

ALTER TABLE build_log
    ADD IF NOT EXISTS stage VARCHAR(255);

ALTER TABLE build_log_messages
    DROP COLUMN IF EXISTS stage;

ALTER TABLE build_log_messages
    DROP COLUMN IF EXISTS status;

ALTER TABLE build_log_messages
    ADD IF NOT EXISTS stage VARCHAR(255);

ALTER TABLE build_log
    ADD IF NOT EXISTS status VARCHAR(255);

ALTER TABLE build_log_messages
    ADD IF NOT EXISTS status VARCHAR(255);

ALTER TABLE build_log
    ADD IF NOT EXISTS trigger VARCHAR(255);