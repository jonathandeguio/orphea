package io.movetodata.accessManager.library.enums;

public enum AccessRequestType {
    ADMINISTRATOR("ADMINISTRATOR"),
    PROJECT("PROJECT");

    private final String displayName;

    AccessRequestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
