package io.bosler.passport.DTO;


import io.bosler.passport.library.models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PermissionMappingWithIdentityAndInheritance {
    private UUID id;
    private UUID resourceId;
    private Role role;
    private Object identity;
    private Boolean inherited;

}
