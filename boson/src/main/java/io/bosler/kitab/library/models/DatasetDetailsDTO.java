package io.bosler.kitab.library.models;

import io.bosler.build.BobEnums.BuildTrigger;
import io.bosler.kitab.library.enums.ResourceStatus;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.kitab.library.enums.ResourceType;
import io.bosler.sharedutils.BoslerUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class DatasetDetailsDTO implements BoslerUtils {
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
