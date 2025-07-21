package org.FRFood;

import com.sun.net.httpserver.HttpServer;
import org.FRFood.HTTPHandler.AuthHandler;
import org.FRFood.HTTPHandler.BuyerHandler;
import org.FRFood.HTTPHandler.RestaurantHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final int port = 8080;

    public static void main(String[] args) {
         try {
         HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
         System.out.println("ok");
         server.createContext("/auth", new AuthHandler());
         server.createContext("/restaurants", new RestaurantHandler());
         server.createContext("/vendors",new BuyerHandler());
         server.setExecutor(Executors.newFixedThreadPool(4));
         server.start();
         System.out.println("Server started. Listening on port " + port);

         Class.forName("com.mysql.cj.jdbc.Driver");
         System.out.println("Connecting to database...");
         } catch (Exception e) {
         System.err.println(e.getMessage());
         }
    }
}