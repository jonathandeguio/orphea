ALTER TABLE deployments
DROP
COLUMN over_ride_time_window;

ALTER TABLE deployments
DROP
COLUMN time_window_end;

ALTER TABLE deployments
DROP
COLUMN time_window_start;

ALTER TABLE deployments
    ADD over_ride_time_window BIGINT;

ALTER TABLE deployments
    ADD time_window_end BIGINT;

ALTER TABLE deployments
    ADD time_window_start BIGINT;