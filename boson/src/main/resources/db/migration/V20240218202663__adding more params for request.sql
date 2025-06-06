ALTER TABLE rest_api_request
    ADD COLUMN IF NOT EXISTS body_type VARCHAR(255);

ALTER TABLE rest_api_request
    ADD COLUMN IF NOT EXISTS method VARCHAR(255);

ALTER TABLE rest_api_request
    ADD COLUMN IF NOT EXISTS raw_body VARCHAR(255);

ALTER TABLE rest_api_request
    ADD COLUMN IF NOT EXISTS query_params VARCHAR(255);

ALTER TABLE rest_api_request
    ADD COLUMN IF NOT EXISTS headers VARCHAR(255);

ALTER TABLE rest_api_request
    ADD COLUMN IF NOT EXISTS form_data VARCHAR(255);