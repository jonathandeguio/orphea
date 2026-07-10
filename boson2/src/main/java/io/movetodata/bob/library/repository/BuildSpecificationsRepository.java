package io.movetodata.bob.library.repository;

import io.movetodata.bob.library.models.BuildSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuildSpecificationsRepository
        extends JpaRepository<BuildSpecification, UUID> {
    BuildSpecification findByDatasetIdAndBranch(UUID datasetId, String branch);

    List<BuildSpecification> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsBuildSpecificationByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsBuildSpecificationByRepositoryAndBranchAndScriptPath(UUID repository, String branch, String scriptPath);

    boolean existsBuildSpecificationByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(UUID datasetId, String branch, UUID repositoryId, String scriptPath, String language);

    List<BuildSpecification> findByRepositoryAndBranchAndScriptPath(UUID repository, String branch, String scriptPath);

    List<BuildSpecification> findAllByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(UUID datasetId, String branch, UUID repositoryId, String scriptPath, String language);

    List<BuildSpecification> findAllByRepositoryAndBranchAndScriptPathAndBuildIdNot(UUID repository, String branch, String scriptPath, String buildId);
}