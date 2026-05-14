package io.orphea.platform.library.repository;

import io.orphea.platform.library.models.BackingFsConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackingFsRepository extends JpaRepository<BackingFsConfigModel, String> {
    BackingFsConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
