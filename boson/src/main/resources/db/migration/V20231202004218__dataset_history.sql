ALTER TABLE dataset_mapping
ADD COLUMN logs_valid_days INTEGER;

ALTER TABLE platform_config
ADD COLUMN logs_valid_days INTEGER;