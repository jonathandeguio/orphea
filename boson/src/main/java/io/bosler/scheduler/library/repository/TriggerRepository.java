package io.bosler.scheduler.library.repository;

import io.bosler.scheduler.library.models.SchedulerJobInfo;
import io.bosler.scheduler.library.models.TriggerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TriggerRepository extends JpaRepository<TriggerModel, UUID> {
}
