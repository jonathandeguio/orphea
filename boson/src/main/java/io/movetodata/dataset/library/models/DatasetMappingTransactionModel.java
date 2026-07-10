package io.movetodata.dataset.library.models;

import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.library.enums.WriteModeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "dataset_mapping_transactions")
public class DatasetMappingTransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID datasetId;
    private String branch;
    @Enumerated(EnumType.STRING)
    private WriteModeEnum writeMode;

    @Enumerated(EnumType.STRING)
    private BuildLaunchedBy launchedBy = BuildLaunchedBy.UNKNOWN;

    @Enumerated(EnumType.STRING)
    private BuildStatus buildStatus = BuildStatus.ACTIVE;

    private UUID buildId;
    private LocalDate localDate;

    @CreatedBy
    private UUID createdBy;
    @CreationTimestamp
    private Date createdAt;
    private Date finishedAt;
}
