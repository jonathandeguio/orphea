ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS highest_time;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS lowest_time;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS mean_time;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS median_time;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS total_aborted;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS total_error;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS total_failed;

ALTER TABLE dataset_mapping
    DROP COLUMN IF EXISTS total_successful;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS highest_time BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS lowest_time BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS mean_time BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS median_time BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS total_aborted BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS total_error BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS total_failed BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS total_successful BIGINT;

ALTER TABLE dataset_mapping
    ADD IF NOT EXISTS total_count BIGINT;