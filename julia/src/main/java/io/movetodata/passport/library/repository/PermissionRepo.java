package io.movetodata.passport.library.repository;

import io.movetodata.passport.library.models.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepo extends JpaRepository<Permissions, Long> {
    Permissions findById(long id);
}
