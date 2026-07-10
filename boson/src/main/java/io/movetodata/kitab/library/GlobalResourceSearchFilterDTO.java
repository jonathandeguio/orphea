package io.movetodata.kitab.library;

import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GlobalResourceSearchFilterDTO {
    private String searchText;
    private ResourceStatus resourceStatus;
    private List<ResourceType> resourceType;
    private Date createdAtTo;
    private Date createdAtFrom;
    private Date updatedAtFrom;
    private Date updatedAtTo;
    private List<UUID> createdBy;
    private List<UUID> resourceIds;
}


