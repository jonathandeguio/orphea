ALTER TABLE kepler_tabs
    ADD bottom_padding INTEGER;

ALTER TABLE kepler_tabs
    ADD canvas_bg VARCHAR(255);

ALTER TABLE kepler_tabs
    ADD chart_body_bg VARCHAR(255);

ALTER TABLE kepler_tabs
    ADD chart_heading_bg VARCHAR(255);

ALTER TABLE kepler_tabs
    ADD chart_heading_text_color VARCHAR(255);

ALTER TABLE kepler_tabs
    ADD left_padding INTEGER;

ALTER TABLE kepler_tabs
    ADD page_bg VARCHAR(255);

ALTER TABLE kepler_tabs
    ADD prevent_collision BOOLEAN;

ALTER TABLE kepler_tabs
    ADD prevent_overlap BOOLEAN;

ALTER TABLE kepler_tabs
    ADD right_padding INTEGER;

ALTER TABLE kepler_tabs
    ADD top_padding INTEGER;

ALTER TABLE kepler_tabs
    ALTER COLUMN bottom_padding SET DEFAULT 2;

-- ALTER TABLE kepler_tabs
--     ALTER COLUMN bottom_padding SET NOT NULL;

ALTER TABLE kepler_tabs
    ALTER COLUMN left_padding SET DEFAULT 2;

-- ALTER TABLE kepler_tabs
--     ALTER COLUMN left_padding SET NOT NULL;

ALTER TABLE kepler_tabs
    ALTER COLUMN prevent_collision SET DEFAULT false;

-- ALTER TABLE kepler_tabs
--     ALTER COLUMN prevent_collision SET NOT NULL;

ALTER TABLE kepler_tabs
    ALTER COLUMN prevent_overlap SET DEFAULT false;

-- ALTER TABLE kepler_tabs
--     ALTER COLUMN prevent_overlap SET NOT NULL;

ALTER TABLE kepler_tabs
    ALTER COLUMN right_padding SET DEFAULT 2;

-- ALTER TABLE kepler_tabs
--     ALTER COLUMN right_padding SET NOT NULL;

ALTER TABLE kepler_tabs
    ALTER COLUMN top_padding SET DEFAULT 2;

-- ALTER TABLE kepler_tabs
--     ALTER COLUMN top_padding SET NOT NULL;