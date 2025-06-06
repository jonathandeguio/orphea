package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.SharepointLinkConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SharepointLinkConfigRepository extends JpaRepository<SharepointLinkConfig, UUID> {
}