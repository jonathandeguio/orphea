package io.movetodata.passport.library.repository;

import io.movetodata.passport.library.models.PermissionsMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionMappingRepo extends JpaRepository<PermissionsMapping, Long> {
    PermissionsMapping findById(long id);
}