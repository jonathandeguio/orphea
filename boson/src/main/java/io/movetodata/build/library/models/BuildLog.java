package io.movetodata.build.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.connect.library.enums.SourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_log")
public class BuildLog {
    @OneToMany(mappedBy = "buildLogStarting", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("startedAt ASC")
    private List<BuildLogMessages> startingLogMessages = new ArrayList<>();

    @OneToMany(mappedBy = "buildLogPreparing", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("startedAt ASC")
    private List<BuildLogMessages> preparingLogMessages = new ArrayList<>();

    @OneToMany(mappedBy = "buildLogRunning", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("startedAt ASC")
    private List<BuildLogMessages> runningLogMessages = new ArrayList<>();

    @OneToMany(mappedBy = "buildLogFinished", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("startedAt ASC")
    private List<BuildLogMessages> finishedLogMessages = new ArrayList<>();


    private @Id
    UUID id;
    private UUID builder;

    @Enumerated(EnumType.STRING)
    private BuildLaunchedBy launchedBy = BuildLaunchedBy.UNKNOWN;

    private String branch;
    private String scriptPath;

    // Checkpoint specific
    private UUID checkpointDataset;
    @Enumerated(EnumType.STRING)
    private BuildStatus checkpointStatus;
    private UUID checkpointTransactionId;

    @Enumerated(EnumType.STRING)
    private BuildTrigger trigger;

    // If, its connect build, then keep the sourceType else its null
    @Enumerated(EnumType.STRING)
    private SourceTypeEnum sourceType;

    private String sparkApplicationId;
    // Global
    @Enumerated(EnumType.STRING)
    private BuildStatus status = BuildStatus.ACTIVE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "build_log_datasetBranchPair", joinColumns = @JoinColumn(name = "owner_id"))
    private Set<DatasetBranchPair> datasetBranchPair = new LinkedHashSet<>();

    // Different Stages are : Starting, Preparing, Running, Finished -> Make enums
    // Different Status are : Active, success, aborted, failed -> Make enums
    @Enumerated(EnumType.STRING)
    private BuildStage stage;
    private UUID startedBy;
    private Date startedAt = new Date();
    private Date finishedAt;
    // Local stages
    private boolean startingStageStatus = false;
    private Date startingStartedAt;
    private Date startingFinishedAt;
    private boolean preparingStageStatus = false;
    private Date preparingStartedAt;
    private Date preparingFinishedAt;
    private boolean runningStageStatus = false;
    private Date runningStartedAt;
    private Date runningFinishedAt;
    private boolean finishedStageStatus = false;
    private Date finishedStartedAt;
    private Date finishedFinishedAt;
}