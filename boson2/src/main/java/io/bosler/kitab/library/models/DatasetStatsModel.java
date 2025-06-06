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
@Table(name = "kitab_dataset_stats", uniqueConstraints = {@UniqueConstraint(columnNames = {"datasetId", "branch"})})
public class DatasetStatsModel {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    UUID datasetId;
    String branch;

    long rows;
    long columns;
    long files;
    long size;

    // TODO : to think about below how to get it, either automatically from other places
    boolean upstreamNewData;
    boolean logicUpdated;


    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
}
