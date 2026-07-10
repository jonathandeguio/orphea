package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.models.DatasetMappingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetMappingRepository
        extends JpaRepository<DatasetMappingModel, DatasetMappingKey> {

}