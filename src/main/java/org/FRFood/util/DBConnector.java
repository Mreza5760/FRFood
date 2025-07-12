package org.FRFood.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String url = "jdbc:mysql://localhost:3306/frdb";
    private static final String user = "root";
    private static final String password = "123456789";

    public static Connection gConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
