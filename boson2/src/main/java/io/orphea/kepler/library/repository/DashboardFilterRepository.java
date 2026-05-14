package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.ChartFilterModel;
import io.orphea.kepler.library.models.DashboardFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DashboardFilterRepository extends JpaRepository<DashboardFilterModel, UUID> {
}