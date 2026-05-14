package io.orphea.kepler.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartPopoverResponseModel {
    private @Id
    UUID id;
    private String name;
    private String description;
    private UUID datasetId;
    private String branch;
    private String path;
    private String projectName;
    @CreationTimestamp
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
