package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.SeriesModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartSeriesRepository extends JpaRepository<SeriesModel, UUID> {


}