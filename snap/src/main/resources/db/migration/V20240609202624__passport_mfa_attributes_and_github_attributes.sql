CREATE TABLE build_git_repository_branch_commit_details
(
    id                                     UUID NOT NULL,
    sha                                    VARCHAR(255),
    url                                    VARCHAR(255),
    created_by                             VARCHAR(255),
    message                                VARCHAR(255),
    created_at                             TIMESTAMP WITHOUT TIME ZONE,
    build_git_repository_branch_details_id UUID,
    CONSTRAINT pk_build_git_repository_branch_commit_details PRIMARY KEY (id)
);

CREATE TABLE build_git_repository_branch_details
(
    id                              UUID NOT NULL,
    branch_name                     VARCHAR(255),
    is_default                      BOOLEAN,
    build_git_repository_details_id UUID,
    CONSTRAINT pk_build_git_repository_branch_details PRIMARY KEY (id)
);

CREATE TABLE build_git_repository_details
(
    id                  UUID NOT NULL,
    name                VARCHAR(255),
    full_name           VARCHAR(255),
    is_private          BOOLEAN,
    description         VARCHAR(255),
    clone_url           VARCHAR(255),
    tags_url            VARCHAR(255),
    commits_url         VARCHAR(255),
    repo_size           INTEGER,
    repository_language VARCHAR(255),
    default_branch      VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    pushed_at           TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_build_git_repository_details PRIMARY KEY (id)
);

CREATE TABLE build_git_repository_tag_details
(
    id                              UUID NOT NULL,
    tag_name                        VARCHAR(255),
    commit_sha                      VARCHAR(255),
    repository_url                  VARCHAR(255),
    created_by                       VARCHAR(255),
    created_at                      TIMESTAMP WITHOUT TIME ZONE,
    build_git_repository_details_id UUID,
    CONSTRAINT pk_build_git_repository_tag_details PRIMARY KEY (id)
);

ALTER TABLE passport_users
    ADD is_mfa_enabled BOOLEAN;

ALTER TABLE passport_users
    ADD mfa_attributes JSON;

ALTER TABLE passport_account_activity
    ADD login_type VARCHAR(255);

ALTER TABLE platform_config
    ADD mfa_enabled BOOLEAN;

ALTER TABLE build_git_repository_branch_commit_details
    ADD CONSTRAINT FK_BUILDGITREPOSITORYBRANCHCOMM_ON_BUILDGITREPOSITORYBRANCHDETA FOREIGN KEY (build_git_repository_branch_details_id) REFERENCES build_git_repository_branch_details (id);

ALTER TABLE build_git_repository_branch_details
    ADD CONSTRAINT FK_BUILDGITREPOSITORYBRANCHDETAILS_ON_BUILDGITREPOSITORYDETAILS FOREIGN KEY (build_git_repository_details_id) REFERENCES build_git_repository_details (id);

ALTER TABLE build_git_repository_tag_details
    ADD CONSTRAINT FK_BUILDGITREPOSITORYTAGDETAILS_ON_BUILDGITREPOSITORYDETAILS FOREIGN KEY (build_git_repository_details_id) REFERENCES build_git_repository_details (id);