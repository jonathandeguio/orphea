package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.ChartMetricModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartMetricRepository extends JpaRepository<ChartMetricModel, UUID> {


}