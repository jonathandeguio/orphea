package io.movetodata.kitab.library.models;

import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.sharedutils.MoveToDataUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class DatasetDetailsDTO implements MoveToDataUtils {
    private UUID id;
    private UUID project;
    private UUID parent;
    private Integer size;
    private String name;
    private String description;
    private ResourceType type;
    private ResourceSubtype subType;
    private ResourceStatus status;
    private boolean favourite;
    private Date createdAt;
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    private UUID buildId;
    private UUID transactionId;
    private BuildTrigger buildTrigger;
}
