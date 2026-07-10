package io.movetodata.build.library.dto;

import io.movetodata.build.BobEnums.BuildLanguage;
import io.movetodata.build.BobEnums.BuildType;
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
public class PreTransformRequest {
    private String branch;
    private UUID repositoryId;
    private String scriptPath;
    private BuildLanguage language;
    private String branchId;
    private String commitId;
    private UUID buildId;
    private String sparkApplicationId;
    private List<SourceDataset> sources;
    private BuildType buildType;
}
