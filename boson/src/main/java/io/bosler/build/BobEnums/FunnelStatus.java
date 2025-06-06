package io.bosler.build.BobEnums;

public enum FunnelStatus {
    INFO("info"),
    FAILED("failed"),
    ABORTED("aborted"),
    ERROR("error");

    private final String displayName;

    FunnelStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
