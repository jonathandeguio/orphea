ALTER TABLE rest_api_request
    ALTER COLUMN query_params TYPE TEXT;

ALTER TABLE rest_api_request
    ALTER COLUMN headers TYPE TEXT;

ALTER TABLE rest_api_request
    ALTER COLUMN form_data TYPE TEXT;

ALTER TABLE rest_api_request
    ALTER COLUMN raw_body TYPE TEXT;

ALTER TABLE webhook_call_data
    ADD COLUMN request_body TEXT;