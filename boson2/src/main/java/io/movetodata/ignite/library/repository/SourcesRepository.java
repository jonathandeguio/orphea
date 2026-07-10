package io.movetodata.ignite.library.repository;

import io.movetodata.ignite.library.models.Sources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SourcesRepository
        extends JpaRepository<Sources, UUID> {

    List<Sources> findAllByAgentId(UUID agentId);

}