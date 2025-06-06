ALTER TABLE deployments
    ADD over_ride_hours INTEGER;

ALTER TABLE deployments
DROP
COLUMN over_ride_time_window;

ALTER TABLE deployments
    ADD over_ride_time_window BIGINT;