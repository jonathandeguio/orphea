package io.orphea.connect.library.repository;

import io.orphea.connect.library.models.FolderSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FolderSourceConfigRepository extends JpaRepository<FolderSourceConfig, UUID> {
}