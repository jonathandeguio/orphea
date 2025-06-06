CREATE TABLE IF NOT EXISTS csv_preprocessing
(
    id                    UUID NOT NULL,
    header                VARCHAR(255),
    field_delimiter       VARCHAR(255),
    trim_space            VARCHAR(255),
    replace_invalid_chars VARCHAR(255),
    date_format           VARCHAR(255),
    time_format           VARCHAR(255),
    timestamp_format      VARCHAR(255),
    CONSTRAINT pk_csv_preprocessing PRIMARY KEY (id)
);

ALTER TABLE connect_webhook
    ADD COLUMN IF NOT EXISTS csv_preprocessing_id UUID;

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS custom_header varchar(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS custom_field_delimiter varchar(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS custom_date_format varchar(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS custom_time_format varchar(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS custom_timestamp_format varchar(255);

ALTER TABLE csv_preprocessing
    ADD IF NOT EXISTS line_delimiter varchar(255);

ALTER TABLE kitab_dataset_custom_schema
    ADD IF NOT EXISTS header BOOLEAN;

ALTER TABLE kitab_dataset_custom_schema
    ADD IF NOT EXISTS quote varchar(255);

ALTER TABLE csv_preprocessing
    ADD IF NOT EXISTS custom_line_delimiter varchar(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS escape VARCHAR(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS quote VARCHAR(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS custom_escape VARCHAR(255);

ALTER TABLE csv_preprocessing
    ADD COLUMN IF NOT EXISTS first_line_as_header boolean;

UPDATE csv_preprocessing
SET line_delimiter = 'NEWLINE'
WHERE line_delimiter IS NULL;

UPDATE kitab_dataset_custom_schema
SET header = TRUE
WHERE header IS NULL;

UPDATE kitab_dataset_custom_schema
SET quote = 'DOUBLE_QUOTE'
WHERE quote IS NULL;

UPDATE csv_preprocessing
SET header = 'NONE'
WHERE header = 'FIRSTLINEHEADER';

UPDATE csv_preprocessing
SET first_line_as_header = TRUE
WHERE first_line_as_header IS NULL;
