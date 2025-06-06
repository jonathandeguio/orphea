package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.FilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilterModelRepository extends JpaRepository<FilterModel, UUID> {
}
