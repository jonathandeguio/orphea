package io.movetodata.scheduler.library.repository;

import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.scheduler.library.models.SchedulerJobInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerJobInfo, UUID> {

    boolean existsByResourceIdAndBranchAndResourceType(UUID resourceId, String branch, ResourceType resourceType);

    List<SchedulerJobInfo> findAllByResourceId(UUID resourceId);

    List<SchedulerJobInfo> findAllByResourceIdAndBranch(UUID resourceId, String branch);

    SchedulerJobInfo findByResourceIdAndBranchAndResourceType(UUID resourceId, String branch, ResourceType resourceType);

    Page<SchedulerJobInfo> findAll(Specification<SchedulerJobInfo> spec, Pageable pageable);
}
