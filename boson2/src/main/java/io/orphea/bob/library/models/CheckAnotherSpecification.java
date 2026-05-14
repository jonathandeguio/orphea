package io.orphea.bob.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CheckAnotherSpecification {
    private UUID datasetId;
    private String branch;
    private String language;
    private String scriptPath;
    private UUID repository;
}
