ALTER TABLE deployments
    ADD over_ride_time_window VARCHAR(255);

ALTER TABLE deployments
    ADD time_window_end VARCHAR(255);

ALTER TABLE deployments
    ADD time_window_start VARCHAR(255);