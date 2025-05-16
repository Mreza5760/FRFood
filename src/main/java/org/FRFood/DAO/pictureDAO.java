package org.FRFood.DAO;

import org.FRFood.util.DatabaseConnector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

public class pictureDAO {

    public void importImage(File imageFile) throws IOException, SQLException {
        String sql = "INSERT INTO PICTURE (image) VALUES (?)";

        try(Connection connection = DatabaseConnector.gConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            FileInputStream fis = new FileInputStream(imageFile);

        }

    }

}
