package io.movetodata.kitab.library.enums;

public enum ResourceStatus {
    ACTIVE("Active"),

    IN_TRASH("In Trash");

    private final String displayName;

    ResourceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
