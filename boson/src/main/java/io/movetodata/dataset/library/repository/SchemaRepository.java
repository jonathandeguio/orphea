package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.models.SchemaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchemaRepository
        extends JpaRepository<SchemaModel, UUID> {

    List<SchemaModel> findByDatasetIdAndBranch(UUID datasetId, String branch);

    List<SchemaModel> findByDatasetIdAndBranchAndTransactionId(UUID datasetId, String branch, UUID transactionId);

    @Modifying
    @Query(value = "DELETE FROM kitab_dataset_schema WHERE dataset_id = :datasetId AND branch = :branch AND transaction_id = :transactionId", nativeQuery = true)
    void deleteAllByDatasetIdAndBranchAndTransactionId(@Param("datasetId") UUID datasetId,
                                                       @Param("branch") String branch,
                                                       @Param("transactionId") UUID transactionId);

    SchemaModel findByDatasetIdAndBranchAndTransactionIdAndStatus(UUID datasetId, String branch, UUID transactionId, String status);

    void deleteByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsByDatasetIdAndBranchAndTransactionId(UUID datasetId, String branch, UUID transactionId);

}