ALTER TABLE kepler_tabs
    DROP
        COLUMN IF exists "order";

ALTER TABLE kepler_tabs
    DROP
        COLUMN IF exists "tab_order";