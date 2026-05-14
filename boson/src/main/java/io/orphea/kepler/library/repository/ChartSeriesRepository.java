package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.SeriesModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartSeriesRepository extends JpaRepository<SeriesModel, UUID> {


}