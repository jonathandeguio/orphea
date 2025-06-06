package io.bosler.kitab.library.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GetByRepositoryProperties {

    private String branch;
    private UUID repositoryId;
    private UUID commitId;
    private String scriptPath;


}
