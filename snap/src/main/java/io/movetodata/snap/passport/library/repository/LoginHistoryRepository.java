package io.movetodata.snap.passport.library.repository;

import io.movetodata.snap.passport.library.models.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, UUID> {
    List<LoginHistory> findByUserId(UUID userId);

    List<LoginHistory> findTop100ByUserIdOrderByLastLoginAtDesc(UUID userId);
}