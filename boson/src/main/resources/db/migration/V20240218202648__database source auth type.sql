UPDATE database_source_config
SET auth_type = COALESCE(auth_type, 'DEFAULT');