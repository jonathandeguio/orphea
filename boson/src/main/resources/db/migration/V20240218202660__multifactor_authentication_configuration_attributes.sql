ALTER TABLE passport_users
    ADD is_mfa_enabled BOOLEAN;

ALTER TABLE passport_users
    ADD mfa_attributes JSON;

ALTER TABLE passport_account_activity
    ADD login_type VARCHAR(255);

ALTER TABLE platform_config
    ADD mfa_enabled BOOLEAN;