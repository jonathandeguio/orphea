package io.bosler.kitab.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_branches", uniqueConstraints = {@UniqueConstraint(columnNames = {"datasetId", "branch", "repositoryId"})})
public class BranchModel {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public String branch = "master";
    public UUID datasetId;
    public UUID repositoryId;
    public String type = "raw";

    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;

}
