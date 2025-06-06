package io.bosler.ignite.library.repository;

import io.bosler.ignite.library.models.IgniteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IgniteConfigRepository extends JpaRepository<IgniteConfig, UUID> {

    IgniteConfig findFirstByOrderByIdDesc();

    IgniteConfig findFirstByAgentIdOrderByIdDesc(UUID agentId);

    IgniteConfig findFirstByAgentIdOrderByUpdatedAtDesc(UUID agentId);

}