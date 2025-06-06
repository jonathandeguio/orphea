ALTER TABLE dataset_mapping_transactions
    ADD branch VARCHAR(255);

ALTER TABLE dataset_mapping_transactions
    ADD dataset_id UUID;

ALTER TABLE dataset_mapping_transactions
    DROP CONSTRAINT fk_dataset_mapping_transactions_on_dadadabr;

ALTER TABLE dataset_mapping_transactions
    DROP COLUMN dataset_mapping_model_branch;

ALTER TABLE dataset_mapping_transactions
    DROP COLUMN dataset_mapping_model_dataset_id;