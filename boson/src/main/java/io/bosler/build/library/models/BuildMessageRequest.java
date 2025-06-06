package io.bosler.build.library.models;

import io.bosler.build.BobEnums.BuildStage;
import io.bosler.build.BobEnums.BuildStatus;
import io.bosler.build.BobEnums.FunnelStatus;
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
