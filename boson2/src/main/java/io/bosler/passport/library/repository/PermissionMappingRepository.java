package io.bosler.passport.library.repository;

import io.bosler.passport.library.models.PermissionsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionMappingRepository extends JpaRepository<PermissionsMapping, UUID> {
    PermissionsMapping findByIdentityIdAndResourceIdAndRoleId(UUID identityId, UUID resourceId, UUID roleId);

    List<PermissionsMapping> findByIdentityId(UUID identityId);

    List<PermissionsMapping> findByResourceId(UUID resourceId);

    List<PermissionsMapping> findByRoleId(UUID roleId);

    boolean existsByIdentityIdAndResourceIdAndRoleId(UUID identityId, UUID resourceId, UUID roleId);

    boolean existsByRoleId(UUID roleId);

    boolean existsByResourceId(UUID resourceId);

    boolean existsByIdentityId(UUID identityId);
}