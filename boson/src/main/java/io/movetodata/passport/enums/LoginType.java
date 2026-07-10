package io.movetodata.passport.enums;

public enum LoginType {
    PLAIN("plain"),
    SSO("sso"),
    MFA("mfa"),
    GOOGLE("google"),
    GITHUB("github");

    private final String displayName;

    LoginType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
