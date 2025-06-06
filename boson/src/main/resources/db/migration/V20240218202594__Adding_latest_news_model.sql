CREATE TABLE home_news
(
    id          UUID    NOT NULL,
    title       TEXT,
    description TEXT,
    status      VARCHAR(255),
    priority    INTEGER NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_home_news PRIMARY KEY (id)
);