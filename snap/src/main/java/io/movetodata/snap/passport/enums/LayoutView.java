package io.movetodata.snap.passport.enums;

public enum LayoutView {
    COMFORTABLE("comfortable"),

    COMPACT("compact");

    private final String displayName;

    LayoutView(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
