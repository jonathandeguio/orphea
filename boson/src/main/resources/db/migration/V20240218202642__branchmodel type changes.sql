UPDATE kitab_branches
SET type = 'RAWDATASET'
WHERE type = 'RAW';

UPDATE kitab_branches
SET type = 'LIVEDATASET'
WHERE type = 'LIVE';

UPDATE kitab_branches
SET type = 'BUILDDATASET'
WHERE type = 'PARQUET';