package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.FilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilterModelRepository extends JpaRepository<FilterModel, UUID> {
}
