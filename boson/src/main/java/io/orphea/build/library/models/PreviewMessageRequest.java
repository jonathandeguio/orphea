package io.orphea.build.library.models;

import io.orphea.build.BobEnums.BuildStage;
import io.orphea.build.BobEnums.FunnelStatus;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.index.qual.SearchIndexBottom;

import java.util.UUID;

@Getter
@Setter
@SearchIndexBottom
public class PreviewMessageRequest {
    BuildStage stage;
    FunnelStatus status;
    String message;
    String debug;
    UUID repositoryId;
    String scriptPath;
}
