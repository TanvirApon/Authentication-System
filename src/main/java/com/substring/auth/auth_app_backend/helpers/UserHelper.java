package com.substring.auth.auth_app_backend.helpers;

import java.util.UUID;

public class UserHelper {

    // Created a simple Helper Methods where I can convert the UUID into String if needed
    public static UUID parseUUID(String uuid){
        return UUID.fromString(uuid);
    }
}
