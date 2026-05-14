package io.orphea.bezier.library.models;

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
@Table(name = "bezier_pipeline")
public class PipelineModel {

    // Declare the fields for each record - these are all the properties of a pet

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public UUID sourceDataset;
    public String sourceBranch;
    public UUID targetDataset;
    public String targetBranch;
    public String repositoryId;
    public String repositoryBranch;
    public String scriptPath;
    public String buildId;
    public String status;
    public String type;
    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;

    public interface TargetDatasetAndTargetBranch{
        UUID getTargetDataset();
        String getTargetBranch();
    }
}
