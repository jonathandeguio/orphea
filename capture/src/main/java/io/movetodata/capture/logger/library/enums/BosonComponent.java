package io.movetodata.capture.logger.library.enums;

public enum BosonComponent {
    ACCESSMANAGER("accessManager"),
    BEZIER("bezier"),
    BUILD("build"),
    COMMENTS("comments"),
    CONNECT("connect"),
    DATASET("dataset"),
    DOCKET("docket"),
    FRACTAL("fractal"),
    KEPLER("kepler"),
    KITAB("kitab"),
    NEWS("news"),
    NOTIFICATIONS("notifications"),
    PASSPORT("passport"),
    PLATFORM("platform"),
    SCHEDULER("scheduler"),
    SUBSCRIPTION("subscription"),
    SYNCHRO("synchro"),
    BOSON("boson");

    private final String displayName;

    BosonComponent(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
