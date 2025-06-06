package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.VersionChangesModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VersionChangesRepository
        extends JpaRepository<VersionChangesModel, UUID> {
}