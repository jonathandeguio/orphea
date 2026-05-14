package io.orphea.platform.library.repository;

import io.orphea.platform.library.models.CacheConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CacheRepository extends JpaRepository<CacheConfigModel, String> {
    CacheConfigModel findByConfig(String config);
    boolean existsByConfig(String config);
}
