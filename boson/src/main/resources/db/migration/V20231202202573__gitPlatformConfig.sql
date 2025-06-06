CREATE TABLE platform_config_git
(
    config   VARCHAR(255) NOT NULL,
    host     VARCHAR(255),
    api_port INTEGER,
    port     INTEGER,
    CONSTRAINT pk_platform_config_git PRIMARY KEY (config)
);

ALTER TABLE connect_agent
DROP
COLUMN created_at;

ALTER TABLE connect_agent
DROP
COLUMN created_by;

ALTER TABLE connect_agent
DROP
COLUMN description;

ALTER TABLE connect_agent
DROP
COLUMN name;

ALTER TABLE connect_agent
DROP
COLUMN parent;

ALTER TABLE connect_agent
DROP
COLUMN updated_at;

ALTER TABLE connect_agent
DROP
COLUMN updated_by;

ALTER TABLE connect_links
DROP
COLUMN created_at;

ALTER TABLE connect_links
DROP
COLUMN created_by;

ALTER TABLE connect_links
DROP
COLUMN description;

ALTER TABLE connect_links
DROP
COLUMN name;

ALTER TABLE connect_links
DROP
COLUMN parent;

ALTER TABLE connect_links
DROP
COLUMN updated_at;

ALTER TABLE connect_links
DROP
COLUMN updated_by;

ALTER TABLE connect_sources
DROP
COLUMN created_at;

ALTER TABLE connect_sources
DROP
COLUMN created_by;

ALTER TABLE connect_sources
DROP
COLUMN description;

ALTER TABLE connect_sources
DROP
COLUMN name;

ALTER TABLE connect_sources
DROP
COLUMN parent;

ALTER TABLE connect_sources
DROP
COLUMN updated_at;

ALTER TABLE connect_sources
DROP
COLUMN updated_by;

ALTER TABLE kepler_charts
DROP
COLUMN created_at;

ALTER TABLE kepler_charts
DROP
COLUMN created_by;

ALTER TABLE kepler_charts
DROP
COLUMN updated_at;

ALTER TABLE kepler_charts
DROP
COLUMN updated_by;

ALTER TABLE kepler_dashboards
DROP
COLUMN created_at;

ALTER TABLE kepler_dashboards
DROP
COLUMN created_by;

ALTER TABLE kepler_dashboards
DROP
COLUMN description;

ALTER TABLE kepler_dashboards
DROP
COLUMN name;

ALTER TABLE kepler_dashboards
DROP
COLUMN parent;

ALTER TABLE kepler_dashboards
DROP
COLUMN type;

ALTER TABLE kepler_dashboards
DROP
COLUMN updated_at;

ALTER TABLE kepler_dashboards
DROP
COLUMN updated_by;

ALTER TABLE kitab_dataset
DROP
COLUMN created_at;

ALTER TABLE kitab_dataset
DROP
COLUMN created_by;

ALTER TABLE kitab_dataset
DROP
COLUMN updated_at;

ALTER TABLE kitab_dataset
DROP
COLUMN updated_by;