package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.ChartCustomizeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartCustomizeRepository extends JpaRepository<ChartCustomizeModel, UUID> {


}