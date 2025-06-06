ALTER TABLE build_trigger
    ADD branch VARCHAR(255);

ALTER TABLE build_trigger
    ADD build_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE build_trigger
    ADD build_by UUID;

ALTER TABLE build_trigger
    ADD build_type VARCHAR(255);

ALTER TABLE build_trigger
    ADD commit_id VARCHAR(255);

ALTER TABLE build_trigger
    ADD config_file_name VARCHAR(255);

ALTER TABLE build_trigger
    ADD config_file_path VARCHAR(255);

ALTER TABLE build_trigger
    ADD harbor_project_name VARCHAR(255);

ALTER TABLE build_trigger
    ADD repo_name VARCHAR(255);

ALTER TABLE build_trigger
    ADD tag VARCHAR(255);

ALTER TABLE build_trigger
DROP
COLUMN last_build_at;

ALTER TABLE build_trigger
DROP
COLUMN last_build_by;

ALTER TABLE build_trigger
DROP
COLUMN latest_commit_id;

ALTER TABLE build_trigger
DROP
COLUMN latest_tag;

ALTER TABLE build_trigger
DROP
COLUMN repository_branch;

ALTER TABLE build_trigger
DROP
COLUMN repository_name;

ALTER TABLE build_trigger
DROP
COLUMN repositoryurl;