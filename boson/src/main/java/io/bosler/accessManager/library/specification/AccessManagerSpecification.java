package io.bosler.accessManager.library.specification;

import io.bosler.accessManager.library.enums.AccessRequestStatus;
import io.bosler.accessManager.library.enums.AccessRequestType;
import io.bosler.accessManager.library.models.AccessRequestModel;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AccessManagerSpecification {
    public static Specification<AccessRequestModel> partialSearchOnId(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText != null && !searchText.isEmpty()) {
                String partialUuid = "%" + searchText + "%";
                return criteriaBuilder.like(root.get("id").as(String.class), partialUuid);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<AccessRequestModel> hasStatus(List<AccessRequestStatus> statuses) {
        return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
    }

    public static Specification<AccessRequestModel> isInRangeFrom(Date rangeFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), rangeFrom);
    }

    public static Specification<AccessRequestModel> isInRangeTo(Date rangeTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), rangeTo);
    }

    public static Specification<AccessRequestModel> typeEquals(AccessRequestType type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<AccessRequestModel> hasRequester(List<UUID> requesters) {
        return (root, query, criteriaBuilder) -> {
            Join<AccessRequestModel, UUID> requestersJoin = root.join("requesters");
            return requestersJoin.in(requesters);
        };
    }

    public static Specification<AccessRequestModel> isMyRequestOnly(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), userId);
    }

    public static Specification<AccessRequestModel> isAssignedToMe(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            Join<AccessRequestModel, UUID> assigneesJoin = root.join("assignees");
            return criteriaBuilder.equal(assigneesJoin, userId);
        };
    }

    public static Specification<AccessRequestModel> hasRequestTarget(List<UUID> requestTargetIds) {
        return (root, query, criteriaBuilder) -> root.get("requestTargetId").in(requestTargetIds);
    }
}
