package io.orphea.platform.library.repository;

import io.orphea.platform.library.models.GitConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitConfigRepository extends JpaRepository<GitConfigModel, String> {
    GitConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
