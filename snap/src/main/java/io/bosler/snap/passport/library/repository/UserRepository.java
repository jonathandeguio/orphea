package io.bosler.snap.passport.library.repository;

import io.bosler.snap.passport.library.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByProviderId(String providerId);

    List<User> findAllByOrderByNameAsc();

    boolean existsByEmail(String email);
}
