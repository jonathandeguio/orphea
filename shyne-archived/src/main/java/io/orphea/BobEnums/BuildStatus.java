package io.orphea.BobEnums;

public enum BuildStatus {
    INFO("info"),
    FAILED("failed"),
    ERROR("error");

    private final String displayName;

    BuildStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
