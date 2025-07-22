package org.FRFood.frontEnd.Util;

import org.FRFood.entity.Order;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static String authToken;
    private static Map<Integer, Order> orderList = new HashMap<Integer, Order>();

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

    public static Map<Integer, Order> getOrderList() {
        return orderList;
    }

}
