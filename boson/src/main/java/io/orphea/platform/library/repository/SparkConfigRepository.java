package io.orphea.platform.library.repository;

import io.orphea.platform.library.models.SparkConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparkConfigRepository extends JpaRepository<SparkConfigModel, String> {
    SparkConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
