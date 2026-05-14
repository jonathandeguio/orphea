package io.orphea.kitab.library.repository;

import io.orphea.kitab.library.keys.HardwareSpecsKey;
import io.orphea.kitab.library.models.RepositoryHardwareSpecsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryHardwareSpecsRepository
        extends JpaRepository<RepositoryHardwareSpecsModel, HardwareSpecsKey> {

    boolean existsById(HardwareSpecsKey id);
}