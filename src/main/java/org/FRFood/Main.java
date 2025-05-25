package org.FRFood;

import org.FRFood.DAO.CategoryDAO;
import org.FRFood.DAO.CategoryDAOImp;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.DatabaseConnector;
import org.FRFood.entity.Category;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        CategoryDAO categoryDAO = new CategoryDAOImp();
        System.out.println("Hello world!");
        try (Connection connection = DatabaseConnector.gConnection()) {
            System.out.println("connected to db");
            try (Statement statement = connection.createStatement()) {


                // test
                try{
                categoryDAO.insertCategory("test1");
                categoryDAO.insertCategory("test2");}
                catch (DataAlreadyExistsException e) {
                    System.out.println(e);
                }
                if(categoryDAO.getCategoryById(2).isPresent()){
                System.out.println(categoryDAO.getCategoryById(2).get().getName());}else{
                    System.out.println("not found with id = 2");
                }
                if(categoryDAO.getCategoryByName("test1").isPresent()){
                    System.out.println(categoryDAO.getCategoryByName("test1").get().getId());
                }else {
                    System.out.println("not found with name = test1");
                }
                // end of test


            }

        } catch (SQLException e) {
            System.out.println("couldn't connect to db" + "\n" + e);
        }
    }
}

