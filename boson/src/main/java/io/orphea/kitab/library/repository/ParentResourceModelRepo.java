package io.orphea.kitab.library.repository;

import io.orphea.kitab.library.models.ParentResourceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParentResourceModelRepo
        extends JpaRepository<ParentResourceModel, UUID> {

}
