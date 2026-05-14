package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepo extends JpaRepository<Users, UUID> {
     Users findByUsername(String username);
}
