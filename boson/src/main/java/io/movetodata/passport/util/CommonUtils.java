package io.movetodata.passport.util;

import java.util.List;
import java.util.UUID;

public class CommonUtils {
    public static final List<String> ADMINSTRATOR_GROUPS = List.of( "platform-administrators", "group-administrators",
            "user-administrators" , "project-administrators", "connect-administrators");
    public static final String PLATFORM_ADMINISTRATOR = "platform-administrator";

    public static boolean isValidUUID(String uuidStr) {
        if (uuidStr == null) {
            return false;
        }
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
