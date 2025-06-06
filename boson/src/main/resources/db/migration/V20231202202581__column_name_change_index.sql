ALTER TABLE sync_index_columns
    ADD column_name VARCHAR(255);

ALTER TABLE sync_index_columns
    DROP COLUMN "column";