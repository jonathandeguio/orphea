package io.bosler.kitab.library.repository;

import io.bosler.kitab.library.models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository
        extends JpaRepository<TransactionModel, UUID> {

    TransactionModel findTransactionModelByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsTransactionModelByDatasetIdAndBranch(UUID datasetId, String branch);

}