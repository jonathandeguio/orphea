package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.SharepointLinkConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SharepointLinkConfigRepository extends JpaRepository<SharepointLinkConfig, UUID> {
}