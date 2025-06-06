package io.bosler.snap.subscribe.library.repository;

import io.bosler.snap.subscribe.library.models.SubscriptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<SubscriptionModel, UUID> {

    List<SubscriptionModel> getByResourceId(UUID resourceId);
    SubscriptionModel findByJobId(UUID jobId);
}