package io.bosler.fractal.library.models;

import io.bosler.kitab.library.enums.ResourceStatus;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.kitab.library.enums.ResourceType;
import io.bosler.sharedutils.DTO.FileNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class RepositoryTreeNode implements FileNode<String> {
    String id;
    UUID project;
    String parent;
    String workspace;
    Integer size = 0;
    String name;
    String description;
    ResourceType type;
    ResourceSubtype subType;
    ResourceStatus status = ResourceStatus.ACTIVE;
    UUID createdBy;
    Date createdAt;
    UUID updatedBy;
    Date updatedAt;
    Boolean favourite = false;
    Set<RepositoryTreeNode> children = new HashSet<>();
    String path;
}
