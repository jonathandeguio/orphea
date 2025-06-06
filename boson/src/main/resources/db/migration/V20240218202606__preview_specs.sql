CREATE TABLE preview_specs
(
    repository_id UUID         NOT NULL,
    row_limit     INTEGER,
    branch        VARCHAR(255) NOT NULL,
    CONSTRAINT pk_preview_specs PRIMARY KEY (repository_id, branch)
);