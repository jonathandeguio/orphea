ALTER TABLE webhook_call_data
    ADD full_url TEXT;

ALTER TABLE webhook_call_data
    ADD extra_errors TEXT;

ALTER TABLE webhook_call_data
    DROP COLUMN response_error;