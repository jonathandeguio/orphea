package io.bosler.build.BobEnums;

public enum BuildStage {
    STARTING("starting"),

    PREPARING("preparing"),

    RUNNING("running"),
    CHECKPOINT("checkpoint"),

    FINISHED("finished");

    private final String displayName;

    BuildStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
