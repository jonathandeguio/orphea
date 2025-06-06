ALTER TABLE access_request
    ADD role VARCHAR(255);

CREATE TABLE access_request_model_assignees
(
    access_request_model_id UUID NOT NULL,
    assignees               UUID
);