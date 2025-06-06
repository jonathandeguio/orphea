package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.SharePointSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SharepointConfigRepository extends JpaRepository<SharePointSourceConfig, UUID> {
}
