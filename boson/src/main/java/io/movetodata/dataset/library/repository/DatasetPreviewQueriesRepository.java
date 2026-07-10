package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.models.DatasetPreviewQueriesModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DatasetPreviewQueriesRepository
        extends JpaRepository<DatasetPreviewQueriesModel, UUID> {

    List<DatasetPreviewQueriesModel> findByDatasetIdOrderByCreatedAtDesc(UUID datasetId);
}