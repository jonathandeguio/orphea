package io.movetodata.zoro.library.repository;

import io.movetodata.zoro.library.models.SchemaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchemaRepository
        extends JpaRepository<SchemaModel, UUID> {

    List<SchemaModel> findByDatasetIdAndBranch(UUID datasetId, String branch);

    SchemaModel findByDatasetIdAndBranchAndStatus(UUID datasetId, String branch, String status);
    void deleteByDatasetIdAndBranch(UUID datasetId, String branch);
    boolean existsByDatasetIdAndBranch(UUID datasetId, String branch);

}