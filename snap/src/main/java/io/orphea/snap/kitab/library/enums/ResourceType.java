package io.orphea.snap.kitab.library.enums;

public enum ResourceType {
    FILE("File"),
    FOLDER("Folder"),
    CHART("Chart"),
    PROJECT("Project"),
    REPOSITORY("Repository"),
    DASHBOARD("Dashboard"),
    CONNECT("Connect"),
    AGENT("Agent"),
    SOURCE("Source"),
    LINK("Link"),
    DATASET("Dataset");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
