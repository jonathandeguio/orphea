package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.ChartFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartFilterRepository extends JpaRepository<ChartFilterModel, UUID> {
}