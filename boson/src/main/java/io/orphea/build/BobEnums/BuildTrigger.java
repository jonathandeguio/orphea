package io.orphea.build.BobEnums;

public enum BuildTrigger {

    COLUMNSTATS("columnstats"),
    SYNCHRO("synchro"),
    CONNECT("connect"),

    UPLOAD("upload"),
    NOTEBOOK("notebook"),
    SQL("sql"),
    PYTHON("python"),

    DATASET("dataset"),

    SCHEDULE("schedule");

    private final String displayName;

    BuildTrigger(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
