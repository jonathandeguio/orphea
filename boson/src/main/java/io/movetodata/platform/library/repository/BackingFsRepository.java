package io.movetodata.platform.library.repository;

import io.movetodata.platform.library.models.BackingFsConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackingFsRepository extends JpaRepository<BackingFsConfigModel, String> {
    BackingFsConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
