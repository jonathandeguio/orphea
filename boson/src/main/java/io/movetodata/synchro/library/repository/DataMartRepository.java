package io.movetodata.synchro.library.repository;

import io.movetodata.platform.library.models.DataMartModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface DataMartRepository extends JpaRepository<DataMartModel, UUID> {
    @Query("SELECT d FROM DataMartModel d WHERE :projectId MEMBER OF d.projects")
    List<DataMartModel> findAllByProject(@Param("projectId") UUID projectId);
}
