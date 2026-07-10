package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository
        extends JpaRepository<TransactionModel, UUID> {
    List<TransactionModel> findAllByDatasetIdAndBranchOrderByCreatedAtDesc(UUID datasetId, String branch);

    List<TransactionModel> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    List<TransactionModel> findAllByDatasetIdAndBranchAndStatusOrderByCreatedAtDesc(UUID datasetId, String branch, String status);

    boolean existsTransactionModelByDatasetIdAndBranchAndStatusOrderByCreatedAtDesc(UUID datasetId, String branch, String status);

    List<TransactionModel> findAllByOrderByCreatedAtDesc();

}