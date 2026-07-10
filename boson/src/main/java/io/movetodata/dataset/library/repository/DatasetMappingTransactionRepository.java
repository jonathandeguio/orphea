package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.models.DatasetMappingTransactionModel;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DatasetMappingTransactionRepository
        extends JpaRepository<DatasetMappingTransactionModel, UUID> {
    List<DatasetMappingTransactionModel> findByDatasetIdAndBranchAndLocalDateOrderByCreatedAtDesc(UUID datasetId, String branch, LocalDate date);

    List<DatasetMappingTransactionModel> findByDatasetIdAndBranchOrderByCreatedAtDesc(UUID datasetId, String branch);

    @Modifying
    @Type(type = "pg-uuid")
    @Query(value = "SELECT CAST(t.id AS varchar) AS id_text " +
            "FROM dataset_mapping_transactions t " +
            "WHERE t.dataset_id = :datasetId " +
            "  AND t.branch = :branch " +
            "  AND t.created_at <= (" +
            "       SELECT t_given.created_at " +
            "       FROM dataset_mapping_transactions t_given " +
            "       WHERE t_given.id = :transactionId" +
            "  ) " +
            "  AND (" +
            "       t.created_at >= (" +
            "           SELECT MAX(t2.created_at) " +
            "           FROM dataset_mapping_transactions t2 " +
            "           WHERE t2.write_mode = 'SNAPSHOT' " +
            "             AND t2.dataset_id = :datasetId " +
            "             AND t2.branch = :branch " +
            "             AND t2.created_at <= (" +
            "                 SELECT t_given.created_at " +
            "                 FROM dataset_mapping_transactions t_given " +
            "                 WHERE t_given.id = :transactionId" +
            "             )" +
            "       ) " +
            "       OR NOT EXISTS (" +
            "           SELECT 1 " +
            "           FROM dataset_mapping_transactions t3 " +
            "           WHERE t3.write_mode = 'SNAPSHOT' " +
            "             AND t3.dataset_id = :datasetId " +
            "             AND t3.branch = :branch " +
            "             AND t3.created_at <= (" +
            "                 SELECT t_given.created_at " +
            "                 FROM dataset_mapping_transactions t_given " +
            "                 WHERE t_given.id = :transactionId" +
            "             )" +
            "       )" +
            "  ) " +
            "ORDER BY t.created_at ASC",
            nativeQuery = true)
    List<String> findTransactionIdsUpToFirstSnapshotOrAll(
            @Param("datasetId") UUID datasetId,
            @Param("branch") String branch,
            @Param("transactionId") UUID transactionId
    );
}