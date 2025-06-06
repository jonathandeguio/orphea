package io.bosler.build.library.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BuildLogRequest {
    private UUID buildId;
    private String sparkApplicationId;
    private String branch;
    private String scriptPath;
}
