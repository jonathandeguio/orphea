package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.models.FolderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FolderRepository
        extends JpaRepository<FolderModel, UUID> {

    List<FolderModel> getByType(String type);

    List<FolderModel> findAllByName(String name);

    List<FolderModel> getByParent(UUID parent);

    FolderModel getByName(String name);

    boolean existsByName(String name);

    FolderModel findByNameAndParentAndStatus(String name, UUID parentId, String status);

    boolean existsByIdAndStatus(UUID Id, String status);

    FolderModel findAllById(UUID id);
}
