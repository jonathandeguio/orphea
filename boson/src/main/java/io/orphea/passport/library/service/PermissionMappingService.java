package io.orphea.passport.library.service;

import io.orphea.passport.library.models.PermissionsMapping;
import io.orphea.passport.library.repository.PermissionMappingRepository;
import io.orphea.passport.library.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionMappingService {
    private final PermissionMappingRepository permissionMappingRepository;
    private final RoleRepository roleRepository;

    public void handleCreatePermissionMapping(UUID ownerId, List<UUID> requesters, UUID resourceId, UUID roleId) {
        for (UUID identity : requesters) {
            log.info("Getting that assigned to a permission mapping:{}", identity);

            PermissionsMapping newPermissionsMapping = new PermissionsMapping();

            if (permissionMappingRepository.existsByIdentityIdAndResourceId(identity, resourceId))
                newPermissionsMapping = permissionMappingRepository.findByIdentityIdAndResourceId(identity, resourceId);

            newPermissionsMapping.setIdentityId(identity);
            newPermissionsMapping.setResourceId(resourceId);
            newPermissionsMapping.setRole(roleRepository.getReferenceById(roleId));

            newPermissionsMapping.setStatus("active");
            newPermissionsMapping.setCreatedBy(ownerId);
            newPermissionsMapping.setCreatedAt(new Date());

            permissionMappingRepository.save(newPermissionsMapping);
            log.info("Permission Mapping was saved:{}", newPermissionsMapping);
        }
    }
}
