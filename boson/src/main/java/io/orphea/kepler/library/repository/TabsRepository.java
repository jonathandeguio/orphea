package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.TabsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TabsRepository
        extends JpaRepository<TabsModel, UUID> {

//    @Modifying
//    @Override
//    @Query("DELETE FROM kepler_tabs where id=:uuid")
//    void deleteById(@Param("uuid") UUID uuid);
}