package io.orphea.build.library.models;

import io.orphea.build.BobEnums.BuildLanguage;
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
    private UUID transactionId;
    private BuildLanguage language;
    private String scriptPath;
    private UUID repository;
}