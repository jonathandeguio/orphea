UPDATE kitab_resource
SET sub_type = 'RAWDATASET'
WHERE sub_type = 'RAW'
  AND type = 'DATASET';

UPDATE kitab_resource
SET sub_type = 'LIVEDATASET'
WHERE sub_type = 'LIVE'
  AND type = 'DATASET';

UPDATE kitab_resource
SET sub_type = 'BUILDDATASET'
WHERE sub_type = 'PARQUET'
  AND type = 'DATASET';

UPDATE kitab_resource
SET sub_type = 'LIVELINK'
WHERE sub_type = 'LIVE'
  AND type = 'LINK';