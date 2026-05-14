package io.orphea.bezier.library.models;

import io.orphea.build.BobEnums.BuildStatus;
import lombok.*;
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
@Builder
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
    public BuildStatus status;
    public String type;
    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;

    public interface TargetDatasetAndTargetBranch {
        UUID getTargetDataset();

        String getTargetBranch();
    }
}
