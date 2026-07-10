package io.movetodata.build.library.dto;

import io.movetodata.build.BobEnums.BuildLanguage;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.library.enums.WriteModeEnum;
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
public class ResolveTargetRequest {
    private List<SourceDataset> sources;
    private String target;
    private String branch;
    private WriteModeEnum writeMode;
    private UUID repositoryId;
    private String scriptPath;
    private BuildLanguage language;
    private BuildTrigger buildTrigger;
    private String branchId;
    private String commitId;
    private UUID buildId;
    private String sparkApplicationId;
    private String fileName;
    private String lineNo;

    // sending userId just in case of notebook build
    private UUID userId;
}
