package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.DatasetFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetFilterModelRepository extends JpaRepository<DatasetFilterModel, UUID> {
}