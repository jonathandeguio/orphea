package io.bosler.snap.logging.library.enums;

public enum LogType {
    INFO("info"),
    WARN("warn"),
    ERROR("error"),
    DEBUG("debug");

    private final String displayName;

    LogType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
