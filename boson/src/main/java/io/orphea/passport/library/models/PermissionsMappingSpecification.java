package io.orphea.passport.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PermissionsMappingSpecification {

    List<UUID> identityId;  // identityId(userId, groupId)
    UUID resourceId;  // resourceId(dataset. folder, project)
    UUID roleId;

}
