package io.orphea.sharedutils.DTO;

import io.orphea.kitab.library.enums.ResourceStatus;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;

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
