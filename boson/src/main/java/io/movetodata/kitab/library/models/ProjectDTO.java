package io.movetodata.kitab.library.models;


import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.sharedutils.MoveToDataUtils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProjectDTO implements MoveToDataUtils {
    private UUID id;
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private ResourceStatus status = ResourceStatus.ACTIVE;

    private Boolean hasAccess;
    private List<UUID> team;

    private UUID createdBy;
    private Date createdAt;
    private UUID updatedBy;
    private Date updatedAt;
}
