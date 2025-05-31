package org.FRFood;

import org.FRFood.DAO.*;
import org.FRFood.entity.*;
import org.FRFood.util.*;


import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        CategoryDAO categoryDAO = new CategoryDAOImp();
        BankAccountDAO bankAccountDAO = new BankAccountDAOImp();
        UserDAO userDAO = new UserDAOImp();
        System.out.println("Hello world!");
        try (Connection connection = DatabaseConnector.gConnection()) {
            System.out.println("connected to db");
            try (Statement statement = connection.createStatement()) {


                // test


                // end of test


            }

        } catch (SQLException e) {
            System.out.println("couldn't connect to db" + "\n" + e);
        }
    }
}

