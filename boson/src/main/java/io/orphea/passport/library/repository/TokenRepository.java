package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.TokenLongLived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<TokenLongLived, Long> {
    TokenLongLived findByName(String name);

    List<TokenLongLived> getByUserId(UUID userId);
}
