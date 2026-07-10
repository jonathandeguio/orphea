package io.movetodata.build.BobEnums;

public enum BuildStatus {
    ABORTED("aborted"),
    
    ACTIVE("active"),
    ERROR("error"),
    FAILED("failed"),

    SUCCESS("success");

    private final String displayName;

    BuildStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
