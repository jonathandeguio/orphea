package io.movetodata.bob.library.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BuildProperties {

    private UUID repositoryId;
    private UUID datasetId;
    private String branch;
    private String branchId;
    private String commitId;
    private String trigger;

    private String scriptPath;
    private int cores = 1;
    private String memory = "512m";
    private int numberOfExecutors = 1;
    private int failureRetries = 1;

}
