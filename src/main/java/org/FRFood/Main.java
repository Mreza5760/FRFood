package org.FRFood;

import com.sun.net.httpserver.HttpServer;
import org.FRFood.HTTPHandler.AuthHandler;


import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int port = 8080;
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/auth", new AuthHandler());
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            System.out.println("Server started. Listening on port " + port);
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("Connecting to database...");
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}