package io.orphea.kitab.library.models;


import io.orphea.kitab.library.enums.ResourceStatus;
import io.orphea.sharedutils.OrpheaUtils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProjectDTO implements OrpheaUtils {
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
