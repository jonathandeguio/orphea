package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ChartsModel;
import io.movetodata.kepler.library.models.DashboardsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardsRepository
        extends JpaRepository<DashboardsModel, UUID> {

//    List<DashboardsModel> findAllByCharts(ChartsModel chartsModel);
}