package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String name);

    List<Role> getByName(String name);

    boolean existsByName(String name);

}