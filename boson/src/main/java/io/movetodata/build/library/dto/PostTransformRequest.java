package io.movetodata.build.library.dto;

import io.movetodata.build.BobEnums.BuildTrigger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostTransformRequest {
    private UUID target;
    private List<SourceDataset> sources;
    private UUID transactionId;
    private String branch;
    private UUID repositoryId;
    private String scriptPath;
    private UUID buildId;
    private BuildTrigger buildTrigger;

    // userId for notebook related builds
    private UUID userId;
}
