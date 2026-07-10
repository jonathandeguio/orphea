package io.movetodata.kepler.enums;

public enum LogicalOperator {
    OR("OR"),
    AND("AND");

    private String name;

    LogicalOperator(String name) {
        this.name = name;
    }
}
