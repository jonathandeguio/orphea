package io.orphea.kitab.library.repository;

import io.orphea.docket.library.models.Tags;
import io.orphea.kitab.library.models.DatasetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetModel, UUID> {
    List<DatasetModel> getByType(String type);

    DatasetModel findDatasetModelById(UUID id);

    List<DatasetModel> findDatasetModelsByTags(Tags tags);
}