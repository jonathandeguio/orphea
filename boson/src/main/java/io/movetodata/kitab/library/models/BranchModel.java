package io.movetodata.kitab.library.models;

import io.movetodata.kitab.library.enums.ResourceSubtype;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_branches", uniqueConstraints = {@UniqueConstraint(columnNames = {"datasetId", "branch", "repositoryId"})})
public class BranchModel {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    public String id;
    public String branch;
    // Remove after BRANCH 2.0
    public UUID datasetId;
    public UUID repositoryId;
//    public String encoding = "none";

    @Enumerated(EnumType.STRING)
    public ResourceSubtype type = ResourceSubtype.RAWDATASET;

    public String buildId;
    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;

}
