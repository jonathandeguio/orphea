package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoginHistoryRepo
        extends JpaRepository<LoginHistory, UUID> {
     List<LoginHistory> findByUserId(UUID userId);
     List<LoginHistory> findTop10ByUserIdOrderByLastLoginAtDesc(UUID userId);
}