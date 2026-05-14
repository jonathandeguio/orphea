package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.ChartConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartConfigRepository extends JpaRepository<ChartConfigModel, UUID> {

    ChartConfigModel findByDatasetIdAndBranch(UUID datasetId, String branch);

}