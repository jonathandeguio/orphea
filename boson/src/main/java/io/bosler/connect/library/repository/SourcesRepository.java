package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SourcesRepository
        extends JpaRepository<Source, UUID> {

    List<Source> findAllByAgentId(UUID agentId);

}