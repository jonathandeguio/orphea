package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.DashboardKey;
import io.bosler.kepler.library.models.DashboardsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.UUID;

@Repository
public interface DashboardsRepository
        extends JpaRepository<DashboardsModel, DashboardKey> {
    ArrayList<DashboardsModel> findById(UUID id);

    void removeAllById(UUID id);
}