UPDATE kitab_resource
SET sub_type = 'STORELINK'
WHERE sub_type = 'STORE'
  AND type = 'LINK';
