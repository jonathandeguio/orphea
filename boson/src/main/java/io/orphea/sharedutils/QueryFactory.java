package io.orphea.sharedutils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;

public class QueryFactory<T> {

    private final EntityManager entityManager;
    private final Class<T> entity;

    public QueryFactory(EntityManager entityManager, Class<T> entity) {
        this.entityManager = entityManager;
        this.entity = entity;
    }

    public List<T> buildQuery(
            Expression<?> groupByExpressions,
            WhereCondition[] conditions,
            int limit,
            List<Order> orderBy
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(entity);

        Root<T> root = query.from(entity);


//        List<Predicate> predicates = new ArrayList<>();
//
//        for (WhereCondition condition : conditions) {
//            switch (condition.getOperator()) {
//                case EQUAL:
//                    predicates.add(criteriaBuilder.equal(root.get(condition.getField()), condition.getValue()));
//                    break;
//                case NOT_EQUAL:
//                    predicates.add(criteriaBuilder.notEqual(root.get(condition.getField()), condition.getValue()));
//                    break;
//            }
//        }
//
//        if (groupByExpressions != null) {
//            criteriaQuery.groupBy(groupByExpressions);
//        }
//        criteriaQuery.where(predicates.toArray(new Predicate[0]));
//
//        if (!orderBy.isEmpty()) {
//            criteriaQuery.orderBy(orderBy);
//        }

        return entityManager.createQuery(query)
                .setMaxResults(limit)
                .getResultList();
    }

    public enum Operator {
        EQUAL, NOT_EQUAL // Add more operators as needed
    }

    @Getter
    @AllArgsConstructor
    public static class WhereCondition {
        private String field;
        private Operator operator;
        private Object value;
    }
}
