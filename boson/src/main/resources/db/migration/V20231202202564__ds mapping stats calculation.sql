ALTER TABLE dataset_mapping
    ADD mean_time INTEGER;

ALTER TABLE dataset_mapping
    ADD total_aborted INTEGER;

ALTER TABLE dataset_mapping
    ADD total_error INTEGER;

ALTER TABLE dataset_mapping
    ADD total_failed INTEGER;

ALTER TABLE dataset_mapping
    ADD total_successful INTEGER;