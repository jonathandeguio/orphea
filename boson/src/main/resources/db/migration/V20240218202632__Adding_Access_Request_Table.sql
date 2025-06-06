CREATE TABLE access_request
(
    id                UUID NOT NULL,
    title             VARCHAR(255),
    description       VARCHAR(255),
    request_target_id UUID,
    status            VARCHAR(255),
    type              VARCHAR(255),
    closing_remarks   VARCHAR(255),
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    closed_at         UUID,
    created_by        UUID,
    updated_by        UUID,
    closed_by         UUID,
    CONSTRAINT pk_access_request PRIMARY KEY (id)
);

CREATE TABLE access_request_model_requesters
(
    access_request_model_id UUID NOT NULL,
    requesters              UUID
);