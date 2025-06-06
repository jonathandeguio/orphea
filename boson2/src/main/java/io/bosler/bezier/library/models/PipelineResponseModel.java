package io.bosler.bezier.library.models;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PipelineResponseModel {
    public @Id
    UUID id;
    public String branch;
    public Integer totalParents;
    public Integer totalChildren;
    public String status;
    public String type;
    public String subType;
    public String repository;
    public String buildStatus;
    public Date buildFinishedAt;
    public String syncStatus;
    public String path;
    public String projectName;
    public String parentFolder;
    public long rows;
    public long columns;
    public long files;
    public long size;

    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
}
