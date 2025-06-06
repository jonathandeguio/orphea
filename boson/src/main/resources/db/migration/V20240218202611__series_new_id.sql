ALTER TABLE kepler_chart_series ADD series_id uuid;

UPDATE kepler_chart_series SET series_id = id;
ALTER TABLE if exists kepler_chart_config_series RENAME COLUMN series_id TO series_series_id;

alter table if exists kepler_chart_config_series drop constraint FKq1o6g5p4u1asi00tf686ubf4l;

ALTER TABLE kepler_chart_series DROP constraint kepler_chart_series_pkey;
ALTER TABLE kepler_chart_series ADD PRIMARY KEY (series_id);

ALTER TABLE kepler_chart_config_series ADD CONSTRAINT FKq1o6g5p4u1asi00tf686ubf4l FOREIGN KEY (series_series_id) REFERENCES kepler_chart_series (series_id);

