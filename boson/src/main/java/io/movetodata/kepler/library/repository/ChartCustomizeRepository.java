package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ChartCustomizeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartCustomizeRepository extends JpaRepository<ChartCustomizeModel, UUID> {


}