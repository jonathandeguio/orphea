package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.Agents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgentRepository
        extends JpaRepository<Agents, UUID> {


}