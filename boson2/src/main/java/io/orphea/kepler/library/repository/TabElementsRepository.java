package io.orphea.kepler.library.repository;

import io.orphea.kepler.library.models.TabElementsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TabElementsRepository
        extends JpaRepository<TabElementsModel, UUID> {

}