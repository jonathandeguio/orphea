package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.models.DataHealth.DataHealthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataHealthRepository
        extends JpaRepository<DataHealthModel, UUID> {
    boolean existsDataHealthModelByDatasetIdAndBranch(UUID datasetId, String branch);

    List<DataHealthModel> findByDatasetIdAndBranch(UUID datasetId, String branch);

}