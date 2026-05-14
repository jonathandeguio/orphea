package io.orphea.bob.library.repository;

import io.orphea.bob.library.models.BuildLog;
import io.orphea.bob.library.models.BuildStageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuildStageLogRepository extends JpaRepository<BuildStageLog, UUID> {
    List<BuildStageLog> findAllByOrderByStartedAtDesc();

    List<BuildStageLog> findAllByDatasetIdAndBranchOrderByStartedAtDesc(UUID datasetId, String branch);
    boolean existsByDatasetIdAndBranchAndBuildLogId(UUID datasetId, String branch, UUID buildLogId);
    BuildStageLog findByDatasetIdAndBranchAndBuildLogId(UUID datasetId, String branch, UUID buildLogId);

    boolean existsByDatasetIdAndBranchAndStatus(UUID datasetId, String branch, String status);
    BuildStageLog findByDatasetIdAndBranchAndStatus(UUID datasetId, String branch, String status);

    List<BuildStageLog> findAllByDatasetId(UUID datasetId);

    boolean existsByBuildLogId(UUID buildLogId);
    BuildStageLog findByBuildLogId(UUID buildLogId);

}