package io.movetodata.bob.library.models;

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

    private String buildStatus;
    private String stageStatus;
    private String sparkApplicationId;
}
