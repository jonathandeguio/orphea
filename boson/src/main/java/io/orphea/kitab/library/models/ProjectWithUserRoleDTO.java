package io.orphea.kitab.library.models;

import io.orphea.passport.library.models.Role;
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
