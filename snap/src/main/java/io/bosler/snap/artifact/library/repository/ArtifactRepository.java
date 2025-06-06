package io.bosler.snap.artifact.library.repository;

import io.bosler.snap.artifact.library.models.ArtifactModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArtifactRepository extends JpaRepository<ArtifactModel, UUID> {

}