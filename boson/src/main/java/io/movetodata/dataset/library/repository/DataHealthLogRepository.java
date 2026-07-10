package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.models.DataHealth.DataHealthLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataHealthLogRepository
        extends JpaRepository<DataHealthLogModel, UUID> {

    List<DataHealthLogModel> findByDatasetIdAndBranch(UUID datasetId, String branch);

    List<DataHealthLogModel> findByHealthCheckId(UUID healthCheckId);

    boolean existsByHealthCheckId(UUID healthCheckId);

}