package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.DatasetMetaDataModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetMetaRepository extends JpaRepository<DatasetMetaDataModel, UUID> {
}
