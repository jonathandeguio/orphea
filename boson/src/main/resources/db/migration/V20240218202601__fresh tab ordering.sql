ALTER TABLE kepler_tabs
    ADD "tab_order" INTEGER;

UPDATE kepler_tabs
SET "tab_order" = 1
WHERE "tab_order" IS NULL;