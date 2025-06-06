package io.bosler.scheduler.library.repository;

import io.bosler.scheduler.library.models.SchedulerJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerJobInfo, Long> {

    SchedulerJobInfo findByJobName(String JobName);

    boolean existsByJobId(String jobId);

    SchedulerJobInfo getByJobId(String jobId);

    void deleteByJobId(String jobId);

    void getByJobId(SchedulerJobInfo getJobInfo);

    SchedulerJobInfo findByDatasetIdAndBranch(UUID datasetId, String branch);

    void deleteBydatasetIdAndBranch(UUID datasetId, String branch);

    SchedulerJobInfo getByJobId(UUID jobId);

    List<SchedulerJobInfo> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    SchedulerJobInfo findJobIdByDatasetIdAndBranchAndTriggerType(UUID datasetId, String branch, String triggerType);

    boolean existsByDatasetIdAndBranchAndTriggerType(UUID datasetId, String branch, String triggerType);

    List<SchedulerJobInfo> findAllByDatasetIdAndBranchAndTriggerType(UUID targetDataset, String targetBranch, String bySource);

    // List<SchedulerJobInfo> findByDatasetIdAndBranch(SchedulerJobInfo datasetId);
}
