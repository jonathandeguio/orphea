ALTER TABLE build_trigger
DROP
COLUMN build_status;

ALTER TABLE build_trigger
    ADD build_status VARCHAR(255);