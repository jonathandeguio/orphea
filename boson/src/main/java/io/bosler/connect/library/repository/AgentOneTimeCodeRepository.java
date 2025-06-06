package io.bosler.connect.library.repository;

import io.bosler.connect.library.models.AgentOneTimeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentOneTimeCodeRepository
        extends JpaRepository<AgentOneTimeCode, UUID> {

    boolean existsByCode(String code);
    Optional<AgentOneTimeCode> findByCode(String code);

}