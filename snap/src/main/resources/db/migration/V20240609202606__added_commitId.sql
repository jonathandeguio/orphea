ALTER TABLE build_artifact
    ADD commit_id VARCHAR(255);

ALTER TABLE build_trigger
    ADD latest_commit_id VARCHAR(255);