CREATE TABLE comment_model_replies
(
    comment_model_id UUID NOT NULL
);

CREATE TABLE kitab_comments
(
    id          UUID NOT NULL,
    resource_id UUID,
    status      VARCHAR(255),
    parent      UUID,
    message     TEXT,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_kitab_comments PRIMARY KEY (id)
);

CREATE TABLE kitab_comments_replies
(
    comment_model_id UUID NOT NULL,
    replies_id       UUID NOT NULL
);

CREATE TABLE kitab_notifications
(
    id          UUID NOT NULL,
    resource_id UUID,
    status      VARCHAR(255),
    message     TEXT,
    is_read     BOOLEAN,
    type        VARCHAR(255),
    influencer  UUID,
    subscriber  UUID,
    timestamp   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_kitab_notifications PRIMARY KEY (id)
);

CREATE TABLE kitab_resource
(
    id            UUID         NOT NULL,
    project       UUID,
    parent_id     UUID,
    workspace     VARCHAR(255),
    size          INTEGER      NOT NULL,
    name          VARCHAR(100),
    description   VARCHAR(255),
    type          VARCHAR(255) NOT NULL,
    sub_type      VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    created_by_id UUID         NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_by_id UUID         NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_kitab_resource PRIMARY KEY (id)
);

CREATE TABLE passport_account_activity
(
    id             UUID NOT NULL,
    user_id        UUID,
    remote_addr    VARCHAR(255),
    agent          VARCHAR(255),
    last_login_at  TIMESTAMP WITHOUT TIME ZONE,
    last_logout_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_passport_account_activity PRIMARY KEY (id)
);

CREATE TABLE passport_groups
(
    id          UUID NOT NULL,
    name        VARCHAR(100),
    description VARCHAR(255),
    status      VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_passport_groups PRIMARY KEY (id)
);

CREATE TABLE passport_groups_managers
(
    groups_id   UUID NOT NULL,
    managers_id UUID NOT NULL
);

CREATE TABLE passport_groups_members
(
    groups_id  UUID NOT NULL,
    members_id UUID NOT NULL
);

CREATE TABLE passport_groups_owners
(
    groups_id UUID NOT NULL,
    owners_id UUID NOT NULL
);

CREATE TABLE passport_long_lived_token
(
    id         UUID    NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    user_id    UUID,
    name       VARCHAR(255),
    expiration TIMESTAMP WITHOUT TIME ZONE,
    status     BOOLEAN NOT NULL,
    CONSTRAINT pk_passport_long_lived_token PRIMARY KEY (id)
);

CREATE TABLE passport_notification_preferences
(
    id           UUID NOT NULL,
    mention      BOOLEAN,
    subscription BOOLEAN,
    CONSTRAINT pk_passport_notification_preferences PRIMARY KEY (id)
);

CREATE TABLE passport_users
(
    id                          UUID         NOT NULL,
    name                        VARCHAR(255),
    username                    VARCHAR(255),
    password                    VARCHAR(255),
    given_name                  VARCHAR(255),
    family_name                 VARCHAR(255),
    location                    VARCHAR(255),
    profile_image               TEXT,
    email                       VARCHAR(255),
    provider                    VARCHAR(255) NOT NULL,
    provider_id                 VARCHAR(255),
    sso_attributes              JSON,
    preferences_id              UUID,
    notification_preferences_id UUID,
    last_login_at               TIMESTAMP WITHOUT TIME ZONE,
    created_at                  TIMESTAMP WITHOUT TIME ZONE,
    updated_at                  TIMESTAMP WITHOUT TIME ZONE,
    created_by                  UUID,
    updated_by                  UUID,
    CONSTRAINT pk_passport_users PRIMARY KEY (id)
);

CREATE TABLE passport_users_preferences
(
    id               UUID NOT NULL,
    cmdopen          BOOLEAN,
    search_open      BOOLEAN,
    map              BOOLEAN,
    font_size        INTEGER,
    language         VARCHAR(255),
    mode             VARCHAR(255),
    timestamp_format VARCHAR(255),
    layout_view      VARCHAR(255),
    auto_formatsql   BOOLEAN,
    folder_list_view BOOLEAN,
    side_panel_open  BOOLEAN,
    hide_files       BOOLEAN,
    CONSTRAINT pk_passport_users_preferences PRIMARY KEY (id)
);

CREATE TABLE permissiondto
(
    id          UUID NOT NULL,
    resource_id UUID,
    identity_id UUID,
    name        VARCHAR(255),
    role        VARCHAR(255),
    CONSTRAINT pk_permissiondto PRIMARY KEY (id)
);

CREATE TABLE platform_config
(
    id              UUID NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    created_by      UUID,
    updated_by      UUID,
    platform_name   VARCHAR(255),
    name            VARCHAR(255),
    logo            TEXT,
    artifactory_url TEXT,
    http_proxy_url  TEXT,
    CONSTRAINT pk_platform_config PRIMARY KEY (id)
);

CREATE TABLE platform_config_smtp
(
    config        VARCHAR(255) NOT NULL,
    smtp_email    VARCHAR(255),
    smtp_password VARCHAR(255),
    host          VARCHAR(255),
    port          INTEGER,
    auth          VARCHAR(255),
    ttls          VARCHAR(255),
    CONSTRAINT pk_platform_config_smtp PRIMARY KEY (config)
);

CREATE TABLE schedule_trigger
(
    trigger_id              UUID NOT NULL,
    trigger_value           VARCHAR(255),
    operator                VARCHAR(255),
    repeat_time             BIGINT,
    last_build              TIMESTAMP WITHOUT TIME ZONE,
    source_updated_by_build BOOLEAN,
    CONSTRAINT pk_schedule_trigger PRIMARY KEY (trigger_id)
);

CREATE TABLE scheduler_job_info
(
    job_id                    UUID NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE,
    updated_at                TIMESTAMP WITHOUT TIME ZONE,
    created_by                UUID,
    updated_by                UUID,
    job_name                  VARCHAR(255),
    resource_id               UUID,
    resource_type             VARCHAR(255),
    branch                    VARCHAR(255),
    job_group                 VARCHAR(255),
    job_status                VARCHAR(255),
    builder                   UUID,
    job_class                 VARCHAR(255),
    trigger_type              VARCHAR(255),
    last_execution            TIMESTAMP WITHOUT TIME ZONE,
    last_job_execution_status VARCHAR(255),
    success_execution_count   BIGINT,
    failure_execution_count   BIGINT,
    CONSTRAINT pk_scheduler_job_info PRIMARY KEY (job_id)
);

CREATE TABLE scheduler_job_info_triggers
(
    scheduler_job_info_job_id UUID NOT NULL,
    triggers_trigger_id       UUID NOT NULL
);

CREATE TABLE subscription
(
    id                 UUID NOT NULL,
    name               VARCHAR(255),
    job_id             UUID,
    send_to            VARCHAR(255),
    subject            VARCHAR(255),
    body               VARCHAR(255),
    resource_type      SMALLINT,
    resource_id        UUID,
    tab_id             UUID,
    cron_expression    VARCHAR(255),
    start_time         VARCHAR(255),
    paused             BOOLEAN,
    preview_image      BOOLEAN,
    provide_permission BOOLEAN,
    created_at         TIMESTAMP WITHOUT TIME ZONE,
    updated_at         TIMESTAMP WITHOUT TIME ZONE,
    created_by         UUID,
    updated_by         UUID,
    CONSTRAINT pk_subscription PRIMARY KEY (id)
);

CREATE TABLE platform_error
(
    id              UUID NOT NULL,
    name            VARCHAR(255),
    message         TEXT,
    stack           TEXT,
    component_stack TEXT,
    CONSTRAINT pk_platform_error PRIMARY KEY (id)
);

ALTER TABLE kitab_comments_replies
    ADD CONSTRAINT uc_kitab_comments_replies_replies UNIQUE (replies_id);

ALTER TABLE scheduler_job_info_triggers
    ADD CONSTRAINT uc_scheduler_job_info_triggers_triggers_triggerid UNIQUE (triggers_trigger_id);

ALTER TABLE passport_users
    ADD CONSTRAINT FK_PASSPORT_USERS_ON_PREFERENCES FOREIGN KEY (preferences_id) REFERENCES passport_users_preferences (id);

ALTER TABLE comment_model_replies
    ADD CONSTRAINT fk_commentmodel_replies_on_comment_model FOREIGN KEY (comment_model_id) REFERENCES kitab_comments (id);

ALTER TABLE kitab_comments_replies
    ADD CONSTRAINT fk_kitcomrep_on_commentmodel FOREIGN KEY (comment_model_id) REFERENCES kitab_comments (id);

ALTER TABLE kitab_comments_replies
    ADD CONSTRAINT fk_kitcomrep_on_replies FOREIGN KEY (replies_id) REFERENCES kitab_comments (id);

ALTER TABLE passport_groups_managers
    ADD CONSTRAINT fk_pasgroman_on_groups FOREIGN KEY (groups_id) REFERENCES passport_groups (id);

ALTER TABLE passport_groups_managers
    ADD CONSTRAINT fk_pasgroman_on_user FOREIGN KEY (managers_id) REFERENCES passport_users (id);

ALTER TABLE passport_groups_members
    ADD CONSTRAINT fk_pasgromem_on_groups FOREIGN KEY (groups_id) REFERENCES passport_groups (id);

ALTER TABLE passport_groups_members
    ADD CONSTRAINT fk_pasgromem_on_user FOREIGN KEY (members_id) REFERENCES passport_users (id);

ALTER TABLE passport_groups_owners
    ADD CONSTRAINT fk_pasgroown_on_groups FOREIGN KEY (groups_id) REFERENCES passport_groups (id);

ALTER TABLE passport_groups_owners
    ADD CONSTRAINT fk_pasgroown_on_user FOREIGN KEY (owners_id) REFERENCES passport_users (id);

ALTER TABLE scheduler_job_info_triggers
    ADD CONSTRAINT fk_schjobinftri_on_scheduler_job_info FOREIGN KEY (scheduler_job_info_job_id) REFERENCES scheduler_job_info (job_id);

ALTER TABLE scheduler_job_info_triggers
    ADD CONSTRAINT fk_schjobinftri_on_trigger_model FOREIGN KEY (triggers_trigger_id) REFERENCES schedule_trigger (trigger_id);