ALTER TABLE build_trigger
    ADD latest_tag VARCHAR(255);

ALTER TABLE build_trigger
DROP
COLUMN tag;