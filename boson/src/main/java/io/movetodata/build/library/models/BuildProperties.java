package io.movetodata.build.library.models;


import io.movetodata.build.BobEnums.BuildLanguage;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.BobEnums.BuildType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BuildProperties {
    private UUID buildId;
    private UUID repositoryId;
    private UUID datasetId;
    private String branch;
    private UUID transactionId;
    private String branchId;
    private String commitId;
    private BuildTrigger trigger;
    private String code;
    private BuildLanguage buildLanguage;
    private BuildType buildType;

    private String rowLimit;
    private String scriptPath;
}
