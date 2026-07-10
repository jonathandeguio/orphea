package io.movetodata.snap.platform.library.repository;

import io.movetodata.snap.platform.library.models.SMTPConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SMTPConfigRepository
        extends JpaRepository<SMTPConfigModel, String> {
    SMTPConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}