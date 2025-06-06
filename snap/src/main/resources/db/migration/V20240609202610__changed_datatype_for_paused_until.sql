ALTER TABLE deployments
    DROP COLUMN paused_until;

ALTER TABLE deployments
    ADD paused_until TIMESTAMP WITHOUT TIME ZONE;