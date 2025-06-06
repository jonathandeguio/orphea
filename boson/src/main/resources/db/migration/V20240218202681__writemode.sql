ALTER TABLE dataset_mapping_transactions
    ADD IF NOT EXISTS write_mode VARCHAR(255);

ALTER TABLE connect_links
    DROP COLUMN IF EXISTS save_mode;

ALTER TABLE connect_links
    ADD IF NOT EXISTS write_mode VARCHAR(255);

UPDATE connect_links
SET write_mode = 'SNAPSHOT'
WHERE write_mode IS NULL;

ALTER TABLE build_specifications
    ADD IF NOT EXISTS write_mode VARCHAR(255);
