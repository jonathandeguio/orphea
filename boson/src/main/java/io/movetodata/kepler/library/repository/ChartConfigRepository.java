package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ChartConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartConfigRepository extends JpaRepository<ChartConfigModel, UUID> {

    ChartConfigModel findByDatasetIdAndBranch(UUID datasetId, String branch);

}