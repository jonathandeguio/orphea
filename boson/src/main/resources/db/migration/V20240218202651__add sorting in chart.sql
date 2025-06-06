ALTER TABLE kepler_chart_config ADD COLUMN sorting_method VARCHAR(40);
ALTER TABLE kepler_chart_config ADD COLUMN sorting_direction VARCHAR(20);
ALTER TABLE kepler_chart_config ADD COLUMN custom_column_name VARCHAR(40);
ALTER TABLE kepler_chart_config ADD COLUMN custom_aggregate_function VARCHAR(20);