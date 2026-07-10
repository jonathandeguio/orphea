package io.movetodata.passport.library.repository;

import io.movetodata.passport.library.models.PermissionsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionMappingRepository extends JpaRepository<PermissionsMapping, UUID> {
    PermissionsMapping findByIdentityIdAndResourceIdAndRoleId(UUID identityId, UUID resourceId, UUID roleId);

    PermissionsMapping findByIdentityIdAndResourceId(UUID identityId, UUID resourceId);

    List<PermissionsMapping> findByIdentityId(UUID identityId);

    List<PermissionsMapping> findByResourceId(UUID resourceId);

    List<PermissionsMapping> findByRoleId(UUID roleId);

    boolean existsByIdentityIdAndResourceIdAndRoleId(UUID identityId, UUID resourceId, UUID roleId);

    boolean existsByIdentityIdAndResourceId(UUID identityId, UUID resourceId);

    boolean existsByRoleId(UUID roleId);

    boolean existsByResourceId(UUID resourceId);

    boolean existsByIdentityId(UUID identityId);
}