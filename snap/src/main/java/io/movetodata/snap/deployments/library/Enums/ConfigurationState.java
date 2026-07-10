package io.movetodata.snap.deployments.library.Enums;

public enum ConfigurationState {
    ACTIVE("active"),
    TARGET("target"),
    ARCHIVED("archived"),
    UNKNOWN("unknown");

    private final String description;

    ConfigurationState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}