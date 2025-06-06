ALTER TABLE dataset_mapping
    ADD CONSTRAINT uk_pk_dataset_mapping unique (dataset_id, branch);

CREATE TABLE dataset_mapping_validDates
(
    owner_id     UUID         NOT NULL,
    owner_branch VARCHAR(255) NOT NULL,
    valid_date   DATE         NOT NULL,
    CONSTRAINT PK_dataset_mapping_validDates PRIMARY KEY (owner_id, owner_branch, valid_date),
    CONSTRAINT FK_dataset_mapping_validDates_owner FOREIGN KEY (owner_id, owner_branch) REFERENCES dataset_mapping (dataset_id, branch)
);

-- CREATE TABLE dataset_mapping_valid_dates
-- (
--     owner_id     UUID         NOT NULL,
--     owner_branch VARCHAR(255) NOT NULL,
--     valid_date   date
-- );
--
-- ALTER TABLE dataset_mapping_valid_dates
--     ADD CONSTRAINT pk_dataset_mapping_valid_dates PRIMARY KEY (owner_id, owner_branch);
--
-- ALTER TABLE dataset_mapping_valid_dates
--     ADD CONSTRAINT fk_dataset_mapping_validdates_on_dataset_mapping_model FOREIGN KEY (owner_id, owner_branch) REFERENCES dataset_mapping (dataset_id, branch);
