package io.movetodata.notifications.library.enums;

public enum NotificationType {
    MENTION("MENTION"),
    ACCESS_REQUEST("ACCESS_REQUEST");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

