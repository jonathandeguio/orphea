package io.orphea.kitab.library.repository;

import io.orphea.kitab.library.models.DatasetMetaDataModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetMetaRepository extends JpaRepository<DatasetMetaDataModel, UUID> {
}
