package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ChartsModel;
import io.movetodata.kepler.library.models.DashboardsModel;
import io.movetodata.kepler.library.models.TabsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TabsRepository
        extends JpaRepository<TabsModel, UUID> {

    @Modifying
    @Override
    @Query("DELETE FROM kepler_tabs where id=:uuid")
    void deleteById(@Param("uuid") UUID uuid);
}