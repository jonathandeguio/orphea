package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.ParentResourceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParentResourceModelRepo
        extends JpaRepository<ParentResourceModel, UUID> {

}
