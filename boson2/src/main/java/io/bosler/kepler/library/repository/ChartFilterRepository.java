package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.ChartFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartFilterRepository extends JpaRepository<ChartFilterModel, UUID> {
}