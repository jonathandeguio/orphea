ALTER TABLE kepler_charts
    ADD COLUMN chart_customize TEXT;
ALTER TABLE kepler_charts
    DROP COLUMN chart_customize_id;