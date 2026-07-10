package io.movetodata.platform.library.repository;

import io.movetodata.platform.library.models.CacheConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CacheRepository extends JpaRepository<CacheConfigModel, String> {
    CacheConfigModel findByConfig(String config);
    boolean existsByConfig(String config);
}
