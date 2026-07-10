package io.movetodata.kepler.library.repository;

import io.movetodata.kepler.library.models.ResourceVersionsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceVersionsRepository
        extends JpaRepository<ResourceVersionsModel, UUID> {

}