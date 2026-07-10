package io.movetodata.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_resource_versions")
public class ResourceVersionsModel {
    @Id
    private UUID resourceId;
    private Long latestVersionId;
    private Long lastVersionId;

    @OneToMany(mappedBy = "resourceVersionsModel")
    @OrderBy("versionId DESC")
    private List<ResourceVersionDetailsModel> versions = new ArrayList<>();

    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
}
