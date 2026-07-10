package io.movetodata.platform.library.repository;

import io.movetodata.platform.library.models.GitConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitConfigRepository extends JpaRepository<GitConfigModel, String> {
    GitConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
