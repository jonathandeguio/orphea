package io.movetodata.accessManager.library.enums;

public enum AccessRequestStatus {
    OPEN("OPEN"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED");

    private final String displayName;

    AccessRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
