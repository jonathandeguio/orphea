package io.movetodata.kepler.library.specifications;

import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChartsSpecifications {

    public static Specification<ResourceModel> partialSearchOnId(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText != null && !searchText.isEmpty()) {
                String partialUuid = "%" + searchText + "%";
                return criteriaBuilder.like(root.get("name").as(String.class), partialUuid);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<ResourceModel> hasTypeChart() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), ResourceType.CHART);
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
