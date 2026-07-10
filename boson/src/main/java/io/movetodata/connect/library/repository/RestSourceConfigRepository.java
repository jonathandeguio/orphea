package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.RestAPISourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestSourceConfigRepository extends JpaRepository<RestAPISourceConfig, UUID> {
}