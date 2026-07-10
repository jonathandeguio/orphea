package io.movetodata.bezier.library.dto;

import io.movetodata.build.library.dto.SourceDataset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResolveBezierLinksDTO {
    public UUID targetDataset;
    public String repositoryBranch;
    private List<SourceDataset> sourceDatasets;
    private String sourceBranch;
    private String targetBranch;
    private String repositoryId;
    private String scriptPath;
    private String buildId;
}
