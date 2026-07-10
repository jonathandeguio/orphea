package io.movetodata.snap.build.library.enums;

public enum BuildType {
    MANUAL("manual"),

    AUTOMATIC("automatic");

    private final String displayName;

    BuildType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}