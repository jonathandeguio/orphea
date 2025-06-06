CREATE TABLE IF NOT EXISTS dataset_mapping_valid_dates
(
    owner_id     UUID         NOT NULL,
    owner_branch VARCHAR(255) NOT NULL,
    valid_date   DATE         NOT NULL,
    CONSTRAINT UK_dataset_mapping_valid_dates UNIQUE (owner_id, owner_branch),
    CONSTRAINT PK_dataset_mapping_valid_dates PRIMARY KEY (owner_id, owner_branch),
    CONSTRAINT FK_dataset_mapping_valid_dates_owner FOREIGN KEY (owner_id, owner_branch) REFERENCES dataset_mapping (dataset_id, branch)
);
