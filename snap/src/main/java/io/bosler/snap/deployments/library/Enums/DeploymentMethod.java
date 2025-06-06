package io.bosler.snap.deployments.library.Enums;

public enum DeploymentMethod {
    MANUAL("manual"),

    AUTOMATIC("automatic"),

    PAUSE("pause");



    private final String displayName;

    DeploymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
