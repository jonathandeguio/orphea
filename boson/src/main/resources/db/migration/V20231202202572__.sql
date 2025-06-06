CREATE TABLE passport_notification_preferences
(
    id           UUID NOT NULL,
    mention      BOOLEAN,
    subscription BOOLEAN,
    CONSTRAINT pk_passport_notification_preferences PRIMARY KEY (id)
);

ALTER TABLE passport_users
    ADD notification_preferences_id UUID;