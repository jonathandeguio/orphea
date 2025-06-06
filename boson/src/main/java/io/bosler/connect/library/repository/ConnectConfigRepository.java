package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.ConnectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConnectConfigRepository extends JpaRepository<ConnectConfig, UUID> {

    ConnectConfig findFirstByOrderByIdDesc();

    ConnectConfig findFirstByAgentIdOrderByIdDesc(UUID agentId);

    ConnectConfig findFirstByAgentIdOrderByUpdatedAtDesc(UUID agentId);

}