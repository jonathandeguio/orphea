package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ChartFilterModel;
import io.movetodata.kepler.library.models.DashboardFilterModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DashboardFilterRepository extends JpaRepository<DashboardFilterModel, UUID> {
}