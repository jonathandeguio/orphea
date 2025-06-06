CREATE TABLE IF NOT EXISTS bezier_pipeline (id uuid not null, build_id varchar(255), created_at timestamp, created_by uuid, repository_branch varchar(255), repository_id varchar(255), script_path varchar(255), source_branch varchar(255), source_dataset uuid, status int4, target_branch varchar(255), target_dataset uuid, type varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS build_log_messages (id uuid not null, debug TEXT, message TEXT, stage int4, started_at timestamp, status int4, build_id_finished uuid, build_id_preparing uuid, build_id_running uuid, build_id_starting uuid, primary key (id));
CREATE TABLE IF NOT EXISTS build_log ( id UUID NOT NULL, builder UUID, branch VARCHAR(255), script_path VARCHAR(255), trigger INTEGER, spark_application_id VARCHAR(255), status INTEGER, stage INTEGER, started_by UUID, started_at TIMESTAMP, finished_at TIMESTAMP, starting_stage_status BOOLEAN, starting_started_at TIMESTAMP, starting_finished_at TIMESTAMP, preparing_stage_status BOOLEAN, preparing_started_at TIMESTAMP, preparing_finished_at TIMESTAMP, running_stage_status BOOLEAN, running_started_at TIMESTAMP, running_finished_at TIMESTAMP, finished_stage_status BOOLEAN, finished_started_at TIMESTAMP, finished_finished_at TIMESTAMP, CONSTRAINT pk_build_log PRIMARY KEY (id) );
CREATE TABLE IF NOT EXISTS build_specifications (id uuid not null, branch varchar(255), branch_id varchar(255), build_id varchar(255), commit_id varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, language varchar(255), repository uuid, script_path varchar(255), transaction_id uuid, updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS chart_popover_response_model (id uuid not null, branch varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, description varchar(255), name varchar(255), path varchar(255), project_name varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS docket_tag_category (id uuid not null, created_at timestamp, created_by uuid, description varchar(255), enabled boolean not null, name varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS docket_tags (id uuid not null, color varchar(255), created_at timestamp, created_by uuid, description varchar(255), name varchar(255), updated_at timestamp, updated_by uuid, tags_category_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS connect_agent (id uuid not null, created_at timestamp, created_by uuid, description varchar(255), http_proxy varchar(255), https_proxy varchar(255), last_status timestamp, name varchar(100), parent uuid, proxy boolean not null, updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS connect_agent_one_time_code (id uuid not null, agent_id uuid, code varchar(255), created_at timestamp, created_by uuid, download_used boolean not null, expiry_time timestamp, install_used boolean not null, primary key (id));
CREATE TABLE IF NOT EXISTS connect_agent_stats (id uuid not null, canonical_host_name varchar(255), created_at timestamp, free_space int8 not null, hostname varchar(255), install_directory varchar(255), ip_address varchar(255), java_version varchar(255), os varchar(255), total_space int8 not null, updated_at timestamp, primary key (id));
CREATE TABLE IF NOT EXISTS connect_config (id uuid not null, agent_id uuid, created_at timestamp, created_by uuid, updated_at timestamp, updated_by uuid, version uuid, primary key (id));
CREATE TABLE connect_links (
  id UUID NOT NULL,
   name VARCHAR(255),
   description VARCHAR(255),
   source_dataset_config BYTEA,
   data_live_load BOOLEAN NOT NULL,
   dataset_id UUID,
   branch VARCHAR(255),
   source_id UUID,
   parent UUID,
   save_mode VARCHAR(255),
   trigger SMALLINT,
   build TIMESTAMP WITHOUT TIME ZONE,
   build_id UUID,
   cron_expression VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,
   created_by UUID,
   updated_by UUID,
   CONSTRAINT pk_connect_links PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS connect_sources (id uuid not null, created_at timestamp, created_by uuid, description varchar(255), direct_load boolean not null, name varchar(255), parent uuid, source_config bytea, updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kepler_chart_config (id uuid not null, branch varchar(255), chart_type varchar(255), dataset_id uuid, latitude varchar(255), longitude varchar(255), map_center varchar(255), map_series varchar(255), map_zoom varchar(255), row_limit int4 not null, x_axis varchar(255), x_axis_sort varchar(255), x_axis_time_grain varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS kepler_chart_config_filter (chart_config_model_id uuid not null, filter_id uuid not null);
CREATE TABLE IF NOT EXISTS kepler_chart_config_series (chart_config_model_id uuid not null, series_id uuid not null);
CREATE TABLE IF NOT EXISTS kepler_chart_customize (id uuid not null, big_number_color varchar(255), big_number_font_size varchar(255), big_number_subheader_color varchar(255), big_number_top varchar(255), color_scheme TEXT, color_theme varchar(255), data_zoom boolean, donut boolean, grid_margin_bottom varchar(255), grid_margin_left varchar(255), grid_margin_right varchar(255), grid_margin_top varchar(255), inner_radius varchar(255), legend boolean, legend_position varchar(255), legend_type varchar(255), line_chart_style varchar(255), map_chart_tile_layer varchar(255), nightangle boolean, outer_radius varchar(255), prefix varchar(255), reversed boolean, scatter_color varchar(255), series_customize TEXT, show_label boolean, sort_bars boolean, stacked_bars boolean, sub_header varchar(255), sub_header_font_size varchar(255), subheader_top varchar(255), suffix varchar(255), tooltip boolean, tooltip_axis_pointer boolean, tooltip_axis_trigger varchar(255), x_axis varchar(255), x_axis_split_line varchar(255), x_axis_title_margin varchar(255), x_axis_title_position varchar(255), y_axis_left varchar(255), y_axis_left_title_margin varchar(255), y_axis_left_title_position varchar(255), y_axis_right varchar(255), y_axis_right_title_margin varchar(255), y_axis_right_title_position varchar(255), y_axis_split_line varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS kepler_chart_filter (id uuid not null, column_name varchar(255) not null, column_type varchar(255) not null, dataset_id uuid, key varchar(255), logical_operator int4 not null, primary key (id));
CREATE TABLE IF NOT EXISTS kepler_chart_filter_filters (dataset_filter_model_id uuid not null, filters_id uuid not null);
CREATE TABLE IF NOT EXISTS kepler_chart_metric (id uuid not null, aggregate varchar(255), column_name varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS kepler_chart_series (id uuid not null, aggregate varchar(255), column_name varchar(255), group_by bytea, reversed boolean, series_customize varchar(255), series_index varchar(255), series_name varchar(255), series_type varchar(255), sort varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS kepler_charts (id uuid not null, version_id int8 not null, branch varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, last_versioned_date timestamp, updated_at timestamp, updated_by uuid, chart_config_id uuid, chart_customize_id uuid, primary key (id, version_id));
CREATE TABLE IF NOT EXISTS kepler_charts_dashboard (charts_id uuid not null, charts_version_id int8 not null, dashboard_id uuid not null, dashboard_version_id int8 not null, primary key (charts_id, charts_version_id, dashboard_id, dashboard_version_id));
CREATE TABLE IF NOT EXISTS kepler_charts_time (id uuid not null, time_column varchar(255), time_grain varchar(255), time_range varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS kepler_dashboards (id uuid not null, version_id int8 not null, branch varchar(255), created_at timestamp, created_by uuid, description varchar(255), last_versioned_date timestamp, name varchar(255), parent uuid, type varchar(255), updated_at timestamp, updated_by uuid, primary key (id, version_id));
CREATE TABLE IF NOT EXISTS kepler_dataset_filter (id uuid not null, key varchar(255), operator varchar(255) not null, value varchar(255) not null, primary key (id));
CREATE TABLE IF NOT EXISTS kepler_tab_elements (id uuid not null, data text, dataset_id varchar(255), position varchar(255), type varchar(255), tabs_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kepler_tabs (id uuid not null, created_at timestamp, created_by uuid, name varchar(255), updated_at timestamp, updated_by uuid, dashboard_id_fk uuid, version_id_fk int8, primary key (id));
CREATE TABLE IF NOT EXISTS kepler_tabs_charts_for_tabs (tabs_for_charts_id uuid not null, charts_for_tabs_id uuid not null, charts_for_tabs_version_id int8 not null, primary key (tabs_for_charts_id, charts_for_tabs_id, charts_for_tabs_version_id));
CREATE TABLE IF NOT EXISTS kitab_branches (id varchar(255) not null, branch varchar(255), build_id varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, encoding varchar(255), repository_id uuid, type int4, updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_comments (id uuid not null, created_at timestamp, created_by uuid, message TEXT, parent uuid, resource_id uuid, status varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_comments_replies (comment_model_id uuid not null, replies_id uuid not null);
CREATE TABLE IF NOT EXISTS kitab_dataset (id uuid not null, created_at timestamp, created_by uuid, dsid uuid, type varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_dataset_branches (dataset_model_id uuid not null, branches_id varchar(255) not null, primary key (dataset_model_id, branches_id));
CREATE TABLE IF NOT EXISTS kitab_dataset_custom_schema (id uuid not null, date_format bytea, display_format bytea, escape_character varchar(255), field_delimiter varchar(255), line_delimiter varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS kitab_dataset_schema (id uuid not null, branch varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, schema bytea, status varchar(255), transaction_id uuid, updated_at timestamp, updated_by uuid, rg_fk uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_dataset_stats (id uuid not null, branch varchar(255), columns int8 not null, created_at timestamp, created_by uuid, dataset_id uuid, files int8 not null, logic_updated boolean not null, rows int8 not null, size int8 not null, updated_at timestamp, updated_by uuid, upstream_new_data boolean not null, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_dataset_tags (dataset_id uuid not null, tag_id uuid not null);
CREATE TABLE IF NOT EXISTS kitab_favourites (id uuid not null, created_at timestamp, resource_id uuid, user_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_folder (id uuid not null, created_at timestamp, description varchar(255), name varchar(100), parent_id uuid, project uuid, size int4 not null, status varchar(255) not null, sub_type varchar(255) not null, type varchar(255) not null, updated_at timestamp, workspace varchar(255), created_by_id uuid not null, updated_by_id uuid not null, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_repository_hardware_specs ( repository UUID NOT NULL, branch VARCHAR(255) NOT NULL, script_path VARCHAR(255) NOT NULL, cores INTEGER NOT NULL, memory VARCHAR(255), number_of_executors INTEGER NOT NULL, failure_retries INTEGER NOT NULL, created_at TIMESTAMP WITHOUT TIME ZONE, updated_at TIMESTAMP WITHOUT TIME ZONE, created_by UUID, updated_by UUID, CONSTRAINT pk_kitab_repository_hardware_specs PRIMARY KEY (repository, branch, script_path) );
CREATE TABLE IF NOT EXISTS kitab_resource_version_details (id uuid not null, created_at timestamp, created_by uuid, name varchar(255), version_id int8, resource_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_resource_version_details_changes_wrapper (id uuid not null, entry_time timestamp, heading varchar(255), user_id uuid, resource_version_details_id_fk uuid, resource_version_details uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_resource_version_details_changes_wrapper_changes (id uuid not null, key varchar(255), treat boolean not null, changes_wrapper_id_fk uuid, changes_wrapper uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_resource_versions (resource_id uuid not null, created_at timestamp, last_version_id int8, latest_version_id int8, updated_at timestamp, primary key (resource_id));
CREATE TABLE IF NOT EXISTS kitab_resource_views (id uuid not null, action varchar(255), resource_id uuid, viewed_at timestamp, viewed_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS kitab_transactions (id uuid not null, branch varchar(255), build_id varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, finished_at timestamp, finished_by uuid, status int4, trigger int4, primary key (id));
CREATE TABLE IF NOT EXISTS passport_account_activity (id uuid not null, agent varchar(255), last_login_at timestamp, last_logout_at timestamp, remote_addr varchar(255), user_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS passport_groups (id uuid not null, created_at timestamp, created_by uuid, description varchar(255), name varchar(100), status varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS passport_groups_managers (groups_id uuid not null, managers_id uuid not null);
CREATE TABLE IF NOT EXISTS passport_groups_members (groups_id uuid not null, members_id uuid not null);
CREATE TABLE IF NOT EXISTS passport_groups_owners (groups_id uuid not null, owners_id uuid not null);
CREATE TABLE IF NOT EXISTS passport_long_lived_token (id uuid not null, created_at timestamp, created_by varchar(255), expiration timestamp, name varchar(255), status boolean not null, updated_at timestamp, updated_by varchar(255), user_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS passport_oauth2_clients (id uuid not null, authorization_uri varchar(255), client_id varchar(255), client_secret varchar(255), created_at timestamp, created_by uuid, description varchar(255), name varchar(255), provider_name varchar(255), redirect_uri varchar(255), registration_id varchar(255), scope varchar(255), status varchar(255), token_uri varchar(255), updated_at timestamp, updated_by uuid, user_info_uri varchar(255), user_name_attribute_name varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS passport_permissions_mapping (id uuid not null, created_at timestamp, created_by uuid, identity_id uuid, resource_id uuid, status varchar(255), updated_at timestamp, updated_by uuid, rg_fk uuid, primary key (id));
CREATE TABLE IF NOT EXISTS passport_role (id uuid not null, delete boolean, name varchar(255), read boolean, status varchar(255), write boolean, primary key (id));
CREATE TABLE IF NOT EXISTS passport_users (id uuid not null, created_at timestamp, created_by uuid, email varchar(255), family_name varchar(255), given_name varchar(255), last_login_at timestamp, location varchar(255), name varchar(255), password varchar(255), profile_image TEXT, provider varchar(255) not null, provider_id varchar(255), sso_attributes json, updated_at timestamp, updated_by uuid, username varchar(255), preferences_id uuid, primary key (id));
CREATE TABLE IF NOT EXISTS passport_users_preferences (id uuid not null, hide_files boolean, cmdopen boolean not null, auto_formatsql boolean not null, folder_list_view boolean not null, font_size int4, language varchar(255), map boolean not null, mode varchar(255), search_open boolean not null, side_panel_open boolean not null, timestamp_format varchar(255), primary key (id));
CREATE TABLE IF NOT EXISTS platform_config (id uuid not null, cache boolean not null, cache_expiration int8, created_at timestamp, created_by uuid, dataset_history int4, download boolean not null, logo TEXT, artifactory_url TEXT,artifactory boolean not null, http_proxy boolean not null, http_proxy_url TEXT, name varchar(255), row_limit int4, size_limit int8, updated_at timestamp, updated_by uuid, upload boolean not null, primary key (id));
CREATE TABLE IF NOT EXISTS platform_config_smtp (config varchar(255) not null, auth varchar(255), host varchar(255), port int4, smtp_email varchar(255), smtp_password varchar(255), ttls varchar(255), primary key (config));
CREATE TABLE IF NOT EXISTS postgres_sync_specification_index_names (postgres_sync_specification_id uuid not null, index_names varchar(255));
CREATE TABLE IF NOT EXISTS schedule_trigger (trigger_id uuid not null, job_class varchar(255), last_build timestamp, operator varchar(255), repeat_time int8, source_updated_by_build boolean, trigger_type varchar(255), trigger_value varchar(255), primary key (trigger_id));
CREATE TABLE IF NOT EXISTS scheduler_job_info ( job_id UUID NOT NULL, created_at TIMESTAMP WITHOUT TIME ZONE, updated_at TIMESTAMP WITHOUT TIME ZONE, created_by UUID, updated_by UUID, job_name VARCHAR(255), resource_id UUID, resource_type SMALLINT, branch VARCHAR(255), job_group VARCHAR(255), job_status SMALLINT, builder UUID, job_class VARCHAR(255), trigger_type SMALLINT, CONSTRAINT pk_scheduler_job_info PRIMARY KEY (job_id) );
CREATE TABLE IF NOT EXISTS scheduler_job_info_triggers ( scheduler_job_info_job_id UUID NOT NULL, triggers_trigger_id UUID NOT NULL );
CREATE TABLE IF NOT EXISTS sources_agent_id (sources_id uuid not null, agent_id uuid);
CREATE TABLE subscription (
  id UUID NOT NULL,
   name VARCHAR(255),
   job_id UUID,
   send_to VARCHAR(255),
   subject VARCHAR(255),
   body VARCHAR(255),
   resource_type SMALLINT,
   resource_id UUID,
   tab_id UUID,
   cron_expression VARCHAR(255),
   start_time VARCHAR(255),
   paused BOOLEAN,
   preview_image BOOLEAN,
   provide_permission BOOLEAN,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,
   created_by UUID,
   updated_by UUID,
   CONSTRAINT pk_subscription PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS synchro_postgres_sync_specifications (id uuid not null, branch varchar(255), created_at timestamp, created_by uuid, dataset_id uuid, enabled boolean not null, finished_at timestamp, started_at timestamp, sync_status varchar(255), synced_by uuid, table_name varchar(255), updated_at timestamp, updated_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS dataset_download_log (id uuid not null, branch varchar(255), dataset_id uuid, downloaded_at timestamp, downloaded_by uuid, number_of_rows int4, size int8, primary key (id));
CREATE TABLE IF NOT EXISTS dataset_mapping ( dataset_id UUID NOT NULL, branch VARCHAR(255) NOT NULL, current_transaction UUID, history_store_type VARCHAR(255), dataset_history INTEGER, CONSTRAINT pk_dataset_mapping PRIMARY KEY (dataset_id) );
CREATE TABLE IF NOT EXISTS dataset_mapping_transactions ( id UUID NOT NULL, status VARCHAR(255), dataset_mapping_model_dataset_id UUID, dataset_mapping_model_branch VARCHAR(255), created_by UUID, created_at TIMESTAMP, CONSTRAINT pk_dataset_mapping_transactions PRIMARY KEY (id) );
CREATE TABLE IF NOT EXISTS dataset_upload_log (id uuid not null, branch varchar(255), dataset_id uuid, uploaded_at timestamp, uploaded_by uuid, primary key (id));
CREATE TABLE IF NOT EXISTS spark_results (id uuid not null, branch varchar(255), column_name varchar(255), dataset_id uuid, results bytea, primary key (id));