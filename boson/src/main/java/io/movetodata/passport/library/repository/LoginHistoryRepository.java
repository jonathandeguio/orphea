package io.movetodata.passport.library.repository;

import io.movetodata.passport.library.models.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, UUID> {
    List<LoginHistory> findByUserId(UUID userId);

    List<LoginHistory> findTop100ByUserIdOrderByLastLoginAtDesc(UUID userId);

    Optional<LoginHistory> findFirstByUserIdAndLastLogoutAtIsNullOrderByLastLoginAtDesc(UUID userId);

    void deleteAllByUserId(UUID userId);
}