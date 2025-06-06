package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.FolderLinkConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FolderLinkConfigRepository extends JpaRepository<FolderLinkConfig, UUID> {
}