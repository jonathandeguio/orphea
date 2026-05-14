package io.orphea.build.library.repository;

import io.orphea.build.library.models.BuildLogMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuildLogMessagesRepository extends JpaRepository<BuildLogMessages, UUID> {

//    List<BuildLogMessages> findByBuildIdOrderByStartedAtDesc(UUID buildId);

}