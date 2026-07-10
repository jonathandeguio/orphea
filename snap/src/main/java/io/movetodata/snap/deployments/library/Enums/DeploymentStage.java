package io.movetodata.snap.deployments.library.Enums;

public enum DeploymentStage {
    STARTING("starting"),

    PREPARING("preparing"),

    RUNNING("running"),
    CHECKPOINT("checkpoint"),

    FINISHED("finished");

    private final String displayName;

    DeploymentStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}