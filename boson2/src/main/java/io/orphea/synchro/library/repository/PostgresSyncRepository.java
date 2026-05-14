package io.orphea.synchro.library.repository;

import io.orphea.synchro.library.models.PostgresSyncSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface PostgresSyncRepository extends JpaRepository<PostgresSyncSpecification, UUID> {

    PostgresSyncSpecification findByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsByDatasetIdAndBranch(UUID datasetId, String branch);

    boolean existsByTableName(String tableName);

    void deleteByDatasetIdAndBranch(UUID datasetId, String branch);

    List<PostgresSyncSpecification> findAllByDatasetIdAndBranch(UUID datasetId, String branch);
}
