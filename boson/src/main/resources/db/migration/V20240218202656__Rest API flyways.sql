CREATE TABLE connect_webhook
(
    id        UUID NOT NULL,
    source_id UUID,
    CONSTRAINT pk_connect_webhook PRIMARY KEY (id)
);

CREATE TABLE connect_webhook_requests
(
    webhook_id  UUID NOT NULL,
    requests_id UUID NOT NULL
);

CREATE TABLE rest_api_request
(
    id        UUID NOT NULL,
    path      VARCHAR(255),
    domain_id UUID,
    CONSTRAINT pk_rest_api_request PRIMARY KEY (id)
);