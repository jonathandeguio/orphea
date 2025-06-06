package io.bosler.platform.library.repository;

import io.bosler.platform.library.models.SparkConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparkConfigRepository extends JpaRepository<SparkConfigModel, String> {
    SparkConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
