package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.FolderSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FolderSourceConfigRepository extends JpaRepository<FolderSourceConfig, UUID> {
}