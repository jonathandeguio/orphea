package io.bosler.bob.library.repository;

import io.bosler.bob.library.models.BuildLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuildLogRepository extends JpaRepository<BuildLog, UUID> {
    List<BuildLog> findAllByOrderByStartedAtDesc();
}