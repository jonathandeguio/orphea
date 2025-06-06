ALTER TABLE rest_api_source_domain
    ALTER COLUMN bearer_token TYPE TEXT;

ALTER TABLE rest_api_source_domain
    ALTER COLUMN api_key_name TYPE TEXT;

ALTER TABLE rest_api_source_domain
    ALTER COLUMN api_key_value TYPE TEXT;