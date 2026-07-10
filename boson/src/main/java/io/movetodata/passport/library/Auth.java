package io.movetodata.passport.library;

import java.util.UUID;

public class Auth {
    public static final String VIEWER = "isViewer(#id)";
    public static final String EDITOR = "isEditor(#id)";
    public static final String OWNER = "isOwner(#id)";
    public static final String CONNECT_ADMIN = "isConnectAdmin()";
    public static final String PLATFORM_ADMIN = "isPlatformAdmin()";

    public static final String PROJECT_ADMIN = "isProjectAdmin()";
    public static final String GROUP_ADMIN = "isGroupAdmin()";

    public static final String PLATFORM_OR_USER_ADMIN = "isPlatformAdmin()";

    public static final String IS_SUBSCRIBER_OR_PLATFORM_ADMIN = "isSubscriberOrPlatformAdmin(#id)";

}