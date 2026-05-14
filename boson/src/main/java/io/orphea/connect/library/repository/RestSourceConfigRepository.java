package io.orphea.connect.library.repository;

import io.orphea.connect.library.models.RestAPISourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestSourceConfigRepository extends JpaRepository<RestAPISourceConfig, UUID> {
}