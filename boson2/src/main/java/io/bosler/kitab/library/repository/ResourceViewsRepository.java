package io.bosler.kitab.library.repository;

import io.bosler.kitab.library.models.ResourceViewsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceViewsRepository
        extends JpaRepository<ResourceViewsModel, UUID> {

    List<ResourceViewsModel> findAllByResourceId(UUID resourceId);

    List<ResourceViewsModel> findFirst10ByViewedByOrderByViewedAtDesc(UUID viewedBy);

    List<ResourceViewsModel> findFirst10DistinctByViewedByOrderByViewedAtDesc(UUID viewedBy);
    // List<ResourceViewsModel> findAllByViewedBy(UUID viewedUserId);
}