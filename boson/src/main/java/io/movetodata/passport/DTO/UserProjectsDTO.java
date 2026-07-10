package io.movetodata.passport.DTO;

import io.movetodata.kitab.library.models.ProjectWithUserRoleDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserProjectsDTO {
    private boolean isProjectOrPlatformAdmin;
    private List<ProjectWithUserRoleDTO> projectList;
}
