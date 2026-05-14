package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.DatasetFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetFilterModelRepository extends JpaRepository<DatasetFilterModel, UUID> {
}