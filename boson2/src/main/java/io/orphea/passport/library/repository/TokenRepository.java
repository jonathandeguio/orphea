package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.Token;
import io.orphea.passport.library.models.TokenLongLived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
//    TokenLongLived findByName(String name);

    List<Token> getByUserId(UUID userId);

    @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(UUID id);

    Optional<Token> findByToken(String token);
}
