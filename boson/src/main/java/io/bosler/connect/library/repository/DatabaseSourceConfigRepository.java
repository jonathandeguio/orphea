package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.DatabaseSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatabaseSourceConfigRepository extends JpaRepository<DatabaseSourceConfig, UUID> {
}