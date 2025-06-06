ALTER TABLE kepler_tabs
    ADD allow_overlap BOOLEAN;

UPDATE kepler_tabs
SET allow_overlap = true
WHERE allow_overlap IS NULL;

ALTER TABLE kepler_tabs
    ALTER COLUMN allow_overlap SET NOT NULL;

ALTER TABLE kepler_tabs
    DROP COLUMN prevent_overlap;

