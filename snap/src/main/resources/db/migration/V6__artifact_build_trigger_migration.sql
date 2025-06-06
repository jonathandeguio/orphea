CREATE TABLE build_artifact
(
    id               UUID NOT NULL,
    tag              VARCHAR(255),
    branch           VARCHAR(255),
    build_status     VARCHAR(255),
    status           BOOLEAN,
    started_at       TIMESTAMP WITHOUT TIME ZONE,
    finished_at      TIMESTAMP WITHOUT TIME ZONE,
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    created_by       UUID,
    updated_by       UUID,
    build_trigger_id UUID,
    CONSTRAINT pk_build_artifact PRIMARY KEY (id)
);

CREATE TABLE build_trigger
(
    id                UUID NOT NULL,
    name              VARCHAR(255),
    description       VARCHAR(255),
    repository_name   VARCHAR(255),
    repositoryurl     VARCHAR(255),
    repository_branch VARCHAR(255),
    latest_tag        VARCHAR(255),
    build_status      VARCHAR(255),
    last_build_at     TIMESTAMP WITHOUT TIME ZONE,
    last_build_by     UUID,
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    created_by        UUID,
    updated_by        UUID,
    CONSTRAINT pk_build_trigger PRIMARY KEY (id)
);

CREATE TABLE build_trigger_event
(
    id          UUID NOT NULL,
    on_push     BOOLEAN,
    on_tag      BOOLEAN,
    branch_name VARCHAR(255),
    tag_name    VARCHAR(255),
    status      BOOLEAN,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_build_trigger_event PRIMARY KEY (id)
);

ALTER TABLE build_artifact
    ADD CONSTRAINT FK_BUILD_ARTIFACT_ON_BUILD_TRIGGER FOREIGN KEY (build_trigger_id) REFERENCES build_trigger (id);