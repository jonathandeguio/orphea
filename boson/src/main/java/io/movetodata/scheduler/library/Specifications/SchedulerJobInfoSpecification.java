package io.movetodata.scheduler.library.Specifications;

import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.scheduler.enums.JobStatus;
import io.movetodata.scheduler.enums.ScheduleTriggerType;
import io.movetodata.scheduler.library.models.SchedulerJobInfo;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public class SchedulerJobInfoSpecification {
    public static Specification<SchedulerJobInfo> partialSearchOnId(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText != null && !searchText.isEmpty()) {
                String partialUuid = "%" + searchText + "%";
                return criteriaBuilder.like(root.get("resourceId").as(String.class), partialUuid);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<SchedulerJobInfo> isNotInTrash() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("resourceStatus"), ResourceStatus.IN_TRASH);
    }

    public static Specification<SchedulerJobInfo> hasTrigger(List<ScheduleTriggerType> scheduleTriggerType) {
        return (root, query, criteriaBuilder) -> root.get("triggerType").in(scheduleTriggerType);
    }

    public static Specification<SchedulerJobInfo> hasStatus(List<JobStatus> jobStatus) {
        return (root, query, criteriaBuilder) -> root.get("jobStatus").in(jobStatus);
    }

    public static Specification<SchedulerJobInfo> isInRangeFrom(Date rangeFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), rangeFrom);
    }

    public static Specification<SchedulerJobInfo> isInRangeTo(Date rangeTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), rangeTo);
    }

    public static Specification<SchedulerJobInfo> isInLastExecutionDateFrom(Date lastExecutionDateFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("lastExecution"), lastExecutionDateFrom);
    }

    public static Specification<SchedulerJobInfo> isInLastExecutionDateTo(Date lastExecutionDateTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("lastExecution"), lastExecutionDateTo);
    }
}
