package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.ChartMetricModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartMetricRepository extends JpaRepository<ChartMetricModel, UUID> {


}