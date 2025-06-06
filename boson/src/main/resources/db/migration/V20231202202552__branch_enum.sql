-- Change the data type
ALTER TABLE kitab_branches
  ALTER COLUMN type TYPE VARCHAR(255);

-- Clear data
UPDATE kitab_branches
SET type = 'PARQUET'
WHERE type = '21';

UPDATE kitab_branches
SET type = 'RAW'
WHERE type = '25';

UPDATE kitab_branches
SET type = 'RAW'
WHERE type = '20';