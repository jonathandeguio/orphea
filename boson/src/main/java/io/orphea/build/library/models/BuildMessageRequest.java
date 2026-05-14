package io.orphea.build.library.models;

import io.orphea.build.BobEnums.BuildStage;
import io.orphea.build.BobEnums.BuildStatus;
import io.orphea.build.BobEnums.FunnelStatus;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.index.qual.SearchIndexBottom;

import java.util.UUID;

@Getter
@Setter
@SearchIndexBottom
public class BuildMessageRequest {
    BuildStage stage;
    FunnelStatus status;
    String message;
    String debug;
    UUID checkpointDataset;
    UUID checkpointTransactionId;
    BuildStatus checkpointStatus;
    String sparkApplicationId;
}
