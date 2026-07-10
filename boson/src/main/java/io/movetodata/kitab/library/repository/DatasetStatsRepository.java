package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.DatasetStatsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@EnableJpaRepositories
public interface DatasetStatsRepository
        extends JpaRepository<DatasetStatsModel, UUID> {

    List<DatasetStatsModel> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    DatasetStatsModel findByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsDatasetStatsModelByDatasetIdAndBranch(UUID datasetId, String branch);
    void deleteByDatasetIdAndBranch(UUID datasetId, String branch);
}