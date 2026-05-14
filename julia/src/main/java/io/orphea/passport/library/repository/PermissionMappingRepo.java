package io.orphea.passport.library.repository;

import io.orphea.passport.library.models.PermissionsMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionMappingRepo extends JpaRepository<PermissionsMapping, Long> {
    PermissionsMapping findById(long id);
}