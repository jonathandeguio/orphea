package io.movetodata.sharedutils.DTO;

import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;

import java.util.Date;
import java.util.UUID;

public interface FileNode<T> {
    T getId();
    UUID getProject();
    T getParent();
    String getWorkspace();
    Integer getSize();
    String getName();
    String getDescription();
    ResourceType getType();
    ResourceSubtype getSubType();
    ResourceStatus getStatus();
    UUID getCreatedBy();
    Date getCreatedAt();
    UUID getUpdatedBy();
    Date getUpdatedAt();
}
