ALTER TABLE database_source_config
    ADD auth_type VARCHAR(255);

ALTER TABLE database_source_config
    ADD private_key VARCHAR(255);

ALTER TABLE database_source_config
    ADD private_key_pass_phrase VARCHAR(255);

ALTER TABLE database_source_config
    ADD schema VARCHAR(255);

ALTER TABLE database_source_config
    ADD user_role VARCHAR(255);

ALTER TABLE database_source_config
    ADD warehouse VARCHAR(255);