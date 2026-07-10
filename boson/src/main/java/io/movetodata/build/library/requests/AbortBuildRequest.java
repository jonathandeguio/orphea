package io.movetodata.build.library.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AbortBuildRequest {
    UUID datasetId;
    String branch;
    UUID buildId;
}
