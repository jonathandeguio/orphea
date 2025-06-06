package io.bosler.build.library.repository;

import io.bosler.build.BobEnums.BuildLanguage;
import io.bosler.build.library.models.BuildSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface BuildSpecificationsRepository
        extends JpaRepository<BuildSpecification, UUID> {

    List<BuildSpecification> findByBuildId(UUID buildId);

    BuildSpecification findByBuildIdAndDatasetId(UUID buildId, UUID datasetId);

    boolean existsBuildSpecificationByBuildId(UUID buildId);

    boolean existsBuildSpecificationByBuildIdAndDatasetId(UUID buildId, UUID datasetId);

    // Used in permanent delete, and check another build spec
    List<BuildSpecification> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    BuildSpecification findByTransactionId(UUID transactionId);

    boolean existsBuildSpecificationByTransactionId(UUID transactionId);

    // Used in check another build spec
    List<BuildSpecification> findAllByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(UUID datasetId, String branch, UUID repositoryId, String scriptPath, BuildLanguage language);

    List<BuildSpecification> findAllByCreatedAtAfter(Date createdAt);
}