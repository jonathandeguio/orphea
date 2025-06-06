ALTER TABLE passport_users
    ADD is_mfa_skipped BOOLEAN;

ALTER TABLE passport_users
    ADD mfa_skipped_at TIMESTAMP WITHOUT TIME ZONE;