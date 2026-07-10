package io.movetodata.build.library.models;

import io.movetodata.build.BobEnums.BuildStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateStatus {
    private UUID repositoryId;
    private String branch;
    private String branchId;
    private String commitId;
    private String scriptPath;
    private UUID datasetId;

    private BuildStatus buildStatus;
    private BuildStatus stageStatus;
    private String sparkApplicationId;
}
