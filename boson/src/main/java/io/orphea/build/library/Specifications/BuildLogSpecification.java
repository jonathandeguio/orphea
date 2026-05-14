package io.orphea.build.library.Specifications;

import io.orphea.build.BobEnums.BuildStatus;
import io.orphea.build.BobEnums.BuildTrigger;
import io.orphea.build.library.models.BuildLog;
import io.orphea.connect.library.enums.SourceTypeEnum;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BuildLogSpecification {

    public static Specification<BuildLog> partialSearchOnId(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText != null && !searchText.isEmpty()) {
                String partialUuid = "%" + searchText + "%";
                return criteriaBuilder.like(root.get("id").as(String.class), partialUuid);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<BuildLog> hasSourceType(List<SourceTypeEnum> sourceTypes) {
        return (root, query, criteriaBuilder) -> root.get("sourceType").in(sourceTypes);
    }

    public static Specification<BuildLog> hasTrigger(List<BuildTrigger> triggers) {
        return (root, query, criteriaBuilder) -> root.get("trigger").in(triggers);
    }

    public static Specification<BuildLog> hasStatus(List<BuildStatus> statuses) {
        return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
    }

    public static Specification<BuildLog> isInRangeFrom(Date rangeFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("startedAt"), rangeFrom);
    }

    public static Specification<BuildLog> isInRangeTo(Date rangeTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("startedAt"), rangeTo);
    }

    public static Specification<BuildLog> isInFinishRangeFrom(Date finishRangeFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("finishedAt"), finishRangeFrom);
    }

    public static Specification<BuildLog> isInFinishRangeTo(Date finishRangeTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("finishedAt"), finishRangeTo);
    }

    public static Specification<BuildLog> startedBy(List<UUID> startedBy) {
        return (root, query, criteriaBuilder) -> root.get("startedBy").in(startedBy);
    }

    public static Specification<BuildLog> hasBranch(String branchFilter) {
        return (root, query, criteriaBuilder) -> {
            if ("2".equals(branchFilter)) {
                return criteriaBuilder.equal(root.get("branch"), "master");
            } else if ("3".equals(branchFilter)) {
                return criteriaBuilder.notEqual(root.get("branch"), "master");
            } else {
                return criteriaBuilder.conjunction(); // No branch filter, return true (no additional condition)
            }
        };
    }

}
