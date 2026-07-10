package io.movetodata.kitab.library.repository;

import io.movetodata.kitab.library.keys.HardwareSpecsKey;
import io.movetodata.kitab.library.models.RepositoryHardwareSpecsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryHardwareSpecsRepository
        extends JpaRepository<RepositoryHardwareSpecsModel, HardwareSpecsKey> {

    boolean existsById(HardwareSpecsKey id);
}