package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.ChartFilterModel;
import io.bosler.kepler.library.models.DashboardFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DashboardFilterRepository extends JpaRepository<DashboardFilterModel, UUID> {
}