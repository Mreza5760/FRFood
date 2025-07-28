package org.FRFood.frontEnd.Util;

import org.FRFood.frontEnd.entity.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static final String SESSION_FILE = "session.dat";
    private static String authToken;
    private static Map<Integer, Order> orderList = new HashMap<Integer, Order>();
    private static User currentUser;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setCurrentUser(User currentUser) {
        SessionManager.currentUser = currentUser;
    }

    public static boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }

    public static void saveSession() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
            out.writeObject(authToken);
            out.writeObject(currentUser);
            out.writeObject(orderList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSession() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            authToken = (String) in.readObject();
            currentUser = (User) in.readObject();
            orderList = (Map<Integer, Order>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void logout() {
        authToken = null;
        orderList.clear();
        currentUser = null;
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Map<Integer, Order> getOrderList() {
        return orderList;
    }
}