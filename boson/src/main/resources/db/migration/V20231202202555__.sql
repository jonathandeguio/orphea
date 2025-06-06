ALTER TABLE dataset_mapping_transactions
    ADD local_date date;

ALTER TABLE dataset_mapping_transactions
    ADD CONSTRAINT FK_DATASET_MAPPING_TRANSACTIONS_ON_DADADABR FOREIGN KEY (dataset_mapping_model_dataset_id, dataset_mapping_model_branch) REFERENCES dataset_mapping (dataset_id, branch);

ALTER TABLE dataset_mapping_validdates
    DROP CONSTRAINT fk_dataset_mapping_validdates_owner;

DROP TABLE dataset_mapping_validdates CASCADE;

ALTER TABLE dataset_mapping_transactions
    DROP COLUMN build_status;

ALTER TABLE dataset_mapping_transactions
    ADD build_status VARCHAR(255);