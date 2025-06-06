package io.bosler.bob.library.models;

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
@Table(name = "bob_build_specifications")
public class BuildSpecification {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public UUID repository;
    public String branch;
    public String branchId;
    public String commitId;

    //    public String sourcesId;
    public UUID datasetId;

    public String scriptPath;
    public String language;

    public String buildId;

    private int cores = 1;
    private String memory = "512m";
    private int numberOfExecutors = 2;
    private int failureRetries = 1;

    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;

    public UUID updatedBy;
    @LastModifiedDate
    public Date updatedAt = new Date();

}
