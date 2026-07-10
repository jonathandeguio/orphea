package io.movetodata.dataset.library.models;

import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.enums.DatasetMappingEnums;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "dataset_mapping")
@IdClass(DatasetMappingKey.class)
public class DatasetMappingModel {
    @Id
    private UUID datasetId;
    @Id
    private String branch;

    private UUID currentTransaction;

    private UUID currentBuildId;

    @Enumerated(EnumType.STRING)
    private DatasetMappingEnums historyStoreType = DatasetMappingEnums.PLATFORM;

    private Integer datasetHistory = 2;
    private Integer logsValidDays = 30;

    @ElementCollection
    @Column(name = "valid_date")
    @CollectionTable(name = "dataset_mapping_valid_dates", joinColumns = {
            @JoinColumn(name = "owner_id", referencedColumnName = "datasetId"),
            @JoinColumn(name = "owner_branch", referencedColumnName = "branch")
    })
    private Set<LocalDate> validDates = new LinkedHashSet<>();

    // Stats Calculation
    private Long totalCount = 0L;
    private Long totalSuccessful = 0L;
    private Long totalFailed = 0L;
    private Long totalError = 0L;
    private Long totalAborted = 0L;
    private Long meanTime = 0L;
    private Long lowestTime = 0L;
    private Long highestTime = 0L;
    private Long medianTime = 0L;

    public void setLogsValidDays(Integer days) {
        if (days == null || days < 2) {
            throw new IllegalArgumentException("Days cannot be null or < 2");
        }
        this.logsValidDays = days;
    }

    public void setDatasetHistory(Integer validHistoricalData) {
        if (validHistoricalData == null || validHistoricalData < 2) {
            throw new IllegalArgumentException("Minimum 2 dataset history must be present.");
        }
        this.datasetHistory = validHistoricalData;
    }
}
