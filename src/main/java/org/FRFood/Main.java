package org.FRFood;

import com.sun.net.httpserver.HttpServer;
import org.FRFood.DAO.*;
import org.FRFood.HTTPHandler.Auth;
import org.FRFood.entity.*;
import org.FRFood.util.*;


import java.net.InetSocketAddress;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Main {
    private static final int port = 8080;
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/auth", new Auth());
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            System.out.println("Server started. Listening on port " + port);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }



//        CategoryDAO categoryDAO = new CategoryDAOImp();
//        BankAccountDAO bankAccountDAO = new BankAccountDAOImp();
//        UserDAO userDAO = new UserDAOImp();
//        System.out.println("Hello world!");
//        try (Connection connection = DatabaseConnector.gConnection()) {
//            System.out.println("connected to db");
//            try (Statement statement = connection.createStatement()) {
//
//
//                // test
//
//
//                // end of test
//
//
//            }
//
//        } catch (SQLException e) {
//            System.out.println("couldn't connect to db" + "\n" + e);
//        }
    }
}

