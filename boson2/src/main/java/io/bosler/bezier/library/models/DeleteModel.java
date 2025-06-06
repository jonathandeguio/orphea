package io.bosler.bezier.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeleteModel {
    private UUID buildId;
    private String scriptPath;
    private UUID repositoryId;
    private String repositoryBranch;
}
