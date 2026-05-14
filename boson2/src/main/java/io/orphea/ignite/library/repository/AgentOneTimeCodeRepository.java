package io.orphea.ignite.library.repository;

import io.orphea.ignite.library.models.AgentOneTimeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgentOneTimeCodeRepository
        extends JpaRepository<AgentOneTimeCode, UUID> {

    boolean existsByCode(String code);

    AgentOneTimeCode findByCode(String code);

}