package com.library.cache;

import java.util.HashMap;

public class SessionCache {
    public static final HashMap<String, String> userData = new HashMap<>();

    public static boolean userAuthorized() {
        return userData.containsKey("userID");
    }
}
