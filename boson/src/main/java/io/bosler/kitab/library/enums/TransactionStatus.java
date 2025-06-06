package io.bosler.kitab.library.enums;

public enum TransactionStatus {
    ACTIVE("active"),
    COMPLETED("completed"),
    ABORTED("aborted");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
