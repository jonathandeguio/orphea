package io.movetodata.kitab.library.specifications;

import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class ResourceSpecification {
    public static Specification<ResourceModel> partialSearchOnName(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText != null && !searchText.isEmpty()) {
                String partialName = "%" + searchText.toLowerCase() + "%";
                // Convert the field value to lower case for the comparison
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name").as(String.class)),
                        partialName
                );
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<ResourceModel> hasSameUUID(List<UUID> resourceIds) {
        return (root, query, criteriaBuilder) -> root.get("id").in(resourceIds);
    }

    public static Specification<ResourceModel> hasStatus(List<ResourceStatus> statuses) {
        return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
    }

    public static Specification<ResourceModel> hasStatus(ResourceStatus status) {
        return (root, query, criteriaBuilder) -> root.get("status").in(status);
    }

    public static Specification<ResourceModel> hasTypes(List<ResourceType> types) {
        return (root, query, criteriaBuilder) -> root.get("type").in(types);
    }

    public static Specification<ResourceModel> isCreatedFrom(Date createdFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
    }

    public static Specification<ResourceModel> isCreatedTo(Date createdTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo);
    }

    public static Specification<ResourceModel> isUpdatedFrom(Date updatedFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom);
    }

    public static Specification<ResourceModel> isUpdatedTo(Date updatedTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), updatedTo);
    }

    public static Specification<ResourceModel> createdBy(List<UUID> createdBy) {
        return (root, query, criteriaBuilder) -> root.get("createdBy").in(createdBy);
    }
}
