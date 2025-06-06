package io.bosler.passport.enums;

public enum AuthRole {
    OWNER("Owner"), EDITOR("Editor"), VIEWER("Viewer");

    private final String displayName;

    AuthRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
