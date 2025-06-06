CREATE TABLE webhook_call_data
(
    id                   UUID NOT NULL,
    url                  VARCHAR(255),
    method               VARCHAR(255),
    request_headers      TEXT,
    response_body        TEXT,
    status               VARCHAR(255),
    execution_started_at TIMESTAMP WITHOUT TIME ZONE,
    execution_ended_at   TIMESTAMP WITHOUT TIME ZONE,
    executed_by          UUID,
    CONSTRAINT pk_webhook_call_data PRIMARY KEY (id)
);

CREATE TABLE webhook_execution_data
(
    id          UUID NOT NULL,
    webhook_id  UUID,
    executed_at TIMESTAMP WITHOUT TIME ZONE,
    executed_by UUID,
    CONSTRAINT pk_webhook_calls_data PRIMARY KEY (id)
);

CREATE TABLE webhook_execution_data_calls
(
    webhook_execution_data_id UUID NOT NULL,
    calls_id                  UUID NOT NULL
);