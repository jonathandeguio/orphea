package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.BranchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository
        extends JpaRepository<BranchModel, String> {
    List<BranchModel> findAllBranchModelByDatasetId(UUID datasetId);

    Optional<BranchModel> findBranchModelByDatasetIdAndBranch(UUID datasetId, String branch);
    BranchModel findBranchModelByDatasetId(UUID datasetId);

    boolean existsByDatasetIdAndBranch(UUID datasetId, String branch);
    boolean existsByDatasetId(UUID datasetId);

    Boolean existsByDatasetIdAndBranchAndRepositoryId(UUID datasetId, String branch, UUID repositoryId);

    Boolean existsByRepositoryIdAndBranch(UUID repositoryId, String branch);

    BranchModel findBranchModelByRepositoryIdAndBranch(UUID repositoryId, String branch);
}