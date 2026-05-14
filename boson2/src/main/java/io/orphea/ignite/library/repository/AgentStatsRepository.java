package io.orphea.ignite.library.repository;

import io.orphea.ignite.library.models.AgentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgentStatsRepository
        extends JpaRepository<AgentStats, UUID> {

}