package io.movetodata.kitab.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.sharedutils.MoveToDataUtils;
import io.movetodata.sharedutils.DTO.FileNode;
import io.movetodata.sharedutils.Serializers.ResourceModelSerializer;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "kitab_resource")
@JsonSerialize(using = ResourceModelSerializer.class)
public class ResourceModel implements FileNode<UUID>, MoveToDataUtils, IResource {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID project;

    @Column(name = "parent_id")
    private UUID parent;

    private String workspace;

    @NotNull
    @Builder.Default
    private Integer size = 0;

    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 100)
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private ResourceType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ResourceSubtype subType;

    private String originalDataType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResourceStatus status = ResourceStatus.ACTIVE;

    @NotNull
    @Column(name = "created_by_id")
    private UUID createdBy;

    @CreationTimestamp
    private Date createdAt;

    @NotNull
    @Column(name = "updated_by_id")
    private UUID updatedBy;

    @LastModifiedDate
    private Date updatedAt;

    @Transient
    @Builder.Default
    private Boolean favourite = false;

    @Transient
    @Builder.Default
    private Set<ResourceModel> children = new HashSet<>();

    public ResourceModel(UUID project, UUID parent, String name, String description, ResourceType type, ResourceSubtype subType, ResourceStatus status, int size, UUID createdBy) {
        this.name = name;
        this.size = size;
        this.description = description;
        this.type = type;
        this.subType = subType;
        this.parent = parent;
        this.status = status;
        this.project = project;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    public ResourceModel(String name, UUID createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

//    @PrePersist
//    public void beforeCreated() {
//        this.createdBy = Objects.requireNonNull(AuthUtils.getCurrentUser()).getId();
//    }

//    @PreUpdate
//    public void beforeUpdate() {
//        this.updatedBy = Objects.requireNonNull(AuthUtils.getCurrentUser()).getId();
//    }
}