package org.FRFood;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

// import com.mysql.cj.x.protobuf.MysqlxCrud.DropView;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        try (Connection connection = DatabaseConnector.gConnection()) {
            System.out.println("connected to db");
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT * FROM Person");
                while (result.next()) {
                    int id = result.getInt("person_id");
                    String name = result.getString("name");
                    System.out.println("ID: " + id + ", Name: " + name);
                }
            }

        } catch (SQLException e) {
            System.out.println("couldn't connect to db" + "\n" + e);
        }
    }
}

class DatabaseConnector {
    private static final String url = "jdbc:mysql://localhost:3306/appdb";
    private static final String user = "root";
    private static final String password = "123456789";

    public static Connection gConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}