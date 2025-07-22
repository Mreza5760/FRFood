package org.FRFood.frontEnd.Util;

public class SessionManager {
    private static String authToken;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }

    public static void logout() {
        authToken = null;
    }
}