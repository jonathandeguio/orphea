package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ChartsModel;
import io.movetodata.kepler.library.models.DashboardsModel;
import io.movetodata.kepler.library.models.TabsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChartsRepository
        extends JpaRepository<ChartsModel, UUID> {

    ChartsModel findByDatasetIdAndBranch(UUID datasetId, String branch);

    List<ChartsModel> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    List<ChartsModel> findAllByDashboardId(UUID datasetId);

}