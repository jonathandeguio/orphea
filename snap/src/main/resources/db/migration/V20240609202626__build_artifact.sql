ALTER TABLE build_trigger
    ADD repo_url VARCHAR(255);

ALTER TABLE build_trigger
    DROP COLUMN config_file_path;