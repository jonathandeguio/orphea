package io.movetodata.synchro.library.repository;

import io.movetodata.synchro.library.models.SyncSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface SyncRepository extends JpaRepository<SyncSpecification, UUID> {
    SyncSpecification findByDatasetIdAndBranchAndDataMartId(UUID datasetId, String branch, UUID sourceId);

    List<SyncSpecification> findByDatasetIdAndBranch(UUID datasetId, String branch);

    List<SyncSpecification> findByDatasetIdAndBranchAndIsDataMartSyncSpec(UUID datasetId, String branch, boolean isDataMartSyncSpec);

    void deleteAllBySourceId(UUID sourceId);
}
