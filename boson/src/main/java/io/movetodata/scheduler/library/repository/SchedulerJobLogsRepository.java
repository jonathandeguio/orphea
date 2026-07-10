package io.movetodata.scheduler.library.repository;

import io.movetodata.scheduler.library.models.SchedulerJobLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchedulerJobLogsRepository extends JpaRepository<SchedulerJobLogs, UUID> {
    Page<SchedulerJobLogs> findByJobId(UUID jobId, Pageable pageable);
}
