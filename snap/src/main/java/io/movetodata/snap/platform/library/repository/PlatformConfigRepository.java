package io.movetodata.snap.platform.library.repository;

import io.movetodata.snap.platform.library.models.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformConfigRepository
        extends JpaRepository<PlatformConfig, UUID> {


    Optional<PlatformConfig> findByName(String name);

    boolean existsByName(String name);

    boolean existsByMfaEnabled(Boolean mfaEnabled);

}