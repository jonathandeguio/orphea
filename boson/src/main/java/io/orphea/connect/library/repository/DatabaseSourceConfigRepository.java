package io.orphea.connect.library.repository;

import io.orphea.connect.library.models.DatabaseSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatabaseSourceConfigRepository extends JpaRepository<DatabaseSourceConfig, UUID> {
}