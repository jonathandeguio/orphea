CREATE TABLE platform_config_cache
(
    config           VARCHAR(255) NOT NULL,
    cache            BOOLEAN      NOT NULL,
    cache_expiration BIGINT,
    use_redis        BOOLEAN      NOT NULL,
    redis_url        VARCHAR(255),
    CONSTRAINT pk_platform_config_cache PRIMARY KEY (config)
);