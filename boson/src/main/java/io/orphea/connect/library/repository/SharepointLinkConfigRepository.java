package io.orphea.connect.library.repository;

import io.orphea.connect.library.models.SharepointLinkConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SharepointLinkConfigRepository extends JpaRepository<SharepointLinkConfig, UUID> {
}