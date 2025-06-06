-- Rename the column
ALTER TABLE dataset_mapping_transactions
  RENAME COLUMN status TO build_status;

-- Clear data
UPDATE dataset_mapping_transactions
SET build_status = 1;

-- Change the data type
ALTER TABLE dataset_mapping_transactions
  ALTER COLUMN build_status TYPE INTEGER USING build_status::INTEGER;