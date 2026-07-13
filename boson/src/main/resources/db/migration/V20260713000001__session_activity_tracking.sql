ALTER TABLE passport_account_activity
    ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS duration_seconds BIGINT,
    ADD COLUMN IF NOT EXISTS end_reason VARCHAR(20);

ALTER TABLE platform_config
    ADD COLUMN IF NOT EXISTS session_timeout_minutes INTEGER DEFAULT 30;

UPDATE platform_config SET session_timeout_minutes = 30 WHERE session_timeout_minutes IS NULL;
