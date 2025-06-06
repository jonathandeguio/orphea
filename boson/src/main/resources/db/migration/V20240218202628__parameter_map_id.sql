drop table if exists kepler_chart_parameters;
drop table if exists kepler_chart_config_parameters;

CREATE TABLE IF NOT EXISTS kepler_chart_parameters (id uuid not null, label VARCHAR(255), columnName VARCHAR(255), primary key (id));
CREATE TABLE IF NOT EXISTS kepler_chart_config_parameters (chart_config_model_id uuid not null, parameters_id uuid not null,  FOREIGN KEY (parameters_id) REFERENCES kepler_chart_parameters(id));
