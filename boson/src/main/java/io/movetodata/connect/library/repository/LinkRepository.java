package io.movetodata.connect.library.repository;

import io.movetodata.connect.library.models.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {

    List<Link> findBySourceId(UUID sourceId);

    List<Link> findBySourceIdAndBuild(UUID sourceId, Date build);

    boolean existsByDatasetId(UUID datasetId);

    boolean existsByDatasetIdAndBranch(UUID datasetId, String branch);

    Optional<Link> findByDatasetIdAndBranch(UUID datasetId, String branch);

}