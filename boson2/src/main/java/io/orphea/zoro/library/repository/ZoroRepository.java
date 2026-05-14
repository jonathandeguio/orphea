package io.orphea.zoro.library.repository;

import io.orphea.zoro.library.models.ZoroModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ZoroRepository
        extends JpaRepository<ZoroModel, UUID> {

    List<ZoroModel> getByType(String type);

    List<ZoroModel> getByName(String name);

    List<ZoroModel> getByParent(UUID parent);


}