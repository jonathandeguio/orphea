package io.orphea.platform.library.repository;

import io.orphea.platform.library.models.SMTPConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SMTPConfigRepository
        extends JpaRepository<SMTPConfigModel, String> {
    SMTPConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}