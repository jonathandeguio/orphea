package io.bosler.snap.passport.library;

public class Auth {
    public static final String VIEWER = "isViewer(#id) or isViewer(@id)";
    public static final String EDITOR = "isEditor(#id) or isEditor(@id)";
    public static final String OWNER = "isOwner(#id) or isOwner(@id)";
    public static final String CONNECT_ADMIN = "isConnectAdmin()";
    public static final String PLATFORM_ADMIN = "isPlatformAdmin()";

    public static final String PLATFORM_OR_USER_ADMIN = "isPlatformAdmin() or isUserAdmin()";

    public static final String IS_SUBSCRIBER_OR_PLATFORM_ADMIN = "isSubscriberOrPlatformAdmin(#id) or isSubscriberOrPlatformAdmin(@id)";

}