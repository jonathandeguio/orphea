package io.bosler.platform.library.models;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "platform_config_cache")
public class CacheConfigModel {
    public @Id
    String config;

    private boolean cache = true;
    private Long cacheExpiration = 2592000L;
    private boolean useRedis = false;
    private String redisUrl;
}
