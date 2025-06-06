package io.bosler.platform.library.repository;

import io.bosler.platform.library.models.SMTPConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SMTPConfigRepository
        extends JpaRepository<SMTPConfigModel, String> {
    SMTPConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}