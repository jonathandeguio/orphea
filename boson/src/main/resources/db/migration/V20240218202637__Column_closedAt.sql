ALTER TABLE access_request
DROP COLUMN closed_at;

ALTER TABLE access_request
    ADD closed_at TIMESTAMP WITHOUT TIME ZONE;
