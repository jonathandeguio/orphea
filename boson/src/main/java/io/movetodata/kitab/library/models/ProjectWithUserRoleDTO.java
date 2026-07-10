package io.movetodata.kitab.library.models;

import io.movetodata.passport.library.models.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ProjectWithUserRoleDTO {
    private UUID id;
    private String name;
    private String description;
    private Role userRole;
}
