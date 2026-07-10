package io.movetodata.ignite.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ignite_links")
public class Links {

    public @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "source data set config is mandatory")
    public String name;
    public String description;

//    public HashMap<String, String> sourceDatasetConfig;
    //@NotEmpty(message = "Target datasetId is mandatory")
    public UUID datasetId;
    @NotEmpty(message = "Target branch is mandatory")
    public String branch = "master";
    //@NotEmpty(message = "sourceId is mandatory")
    public UUID sourceId;
    public UUID parent;

    public String saveMode = "overwrite";  // overwrite , append, update
    public String trigger = "None";  // none, cron, watcher
    public Date build;  // this should be used to launch builds
    public String cronExpression;

    @CreationTimestamp
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;
}
