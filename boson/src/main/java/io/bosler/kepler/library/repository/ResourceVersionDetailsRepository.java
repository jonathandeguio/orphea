package io.bosler.kepler.library.repository;

import io.bosler.kepler.library.models.ResourceVersionDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceVersionDetailsRepository
        extends JpaRepository<ResourceVersionDetailsModel, UUID> {
//    ResourceVersionDetailsModel findByResourceIdAndVersionId(UUID resourceId, Long versionId);
}