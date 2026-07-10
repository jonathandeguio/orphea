package io.movetodata.ignite.library.repository;

import io.movetodata.ignite.library.models.Links;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface LinkRepository extends JpaRepository<Links, UUID> {

    List<Links> findBySourceId(UUID sourceId);

    List<Links> findBySourceIdAndBuild(UUID sourceId, Date build);

    boolean existsByDatasetId(UUID datasetId);

    boolean existsByDatasetIdAndBranch(UUID datasetId, String branch);

    Links findByDatasetIdAndBranch(UUID datasetId, String branch);

}