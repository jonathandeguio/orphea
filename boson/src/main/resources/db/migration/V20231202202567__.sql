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