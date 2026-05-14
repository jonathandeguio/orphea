package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.ChartKey;
import io.orphea.kepler.library.models.ChartsModel;
import io.orphea.kepler.library.models.DashboardsModel;
import io.orphea.kitab.library.models.ResourceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChartsRepository
        extends JpaRepository<ChartsModel, ChartKey> {

    ArrayList<ChartsModel> findById(UUID id);

    void removeAllById(UUID id);

    ChartsModel findByDatasetIdAndBranch(UUID datasetId, String branch);

    List<ChartsModel> findAllByDatasetIdAndBranch(UUID datasetId, String branch);

    List<ChartsModel> findAllByDashboardId(UUID datasetId);

    Page<ChartsModel> findAll(Specification<ChartsModel> spec, Pageable suggestedCharts);
}