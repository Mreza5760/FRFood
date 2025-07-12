package org.FRFood.DAO;

import org.FRFood.util.DBConnector;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class PictureDAO {

    public int importImage(String urlToPic) {
        String sql = "INSERT INTO PICTURE (image) VALUES (?)";

        try (Connection connection = DBConnector.gConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            URL picUrl = new URL(urlToPic);
            File tempImage = File.createTempFile("food_pic", ".jpg");
            Files.copy(picUrl.openStream(), tempImage.toPath(), StandardCopyOption.REPLACE_EXISTING);

            FileInputStream fis = new FileInputStream(tempImage);
            statement.setBinaryStream(1, fis, (int) tempImage.length());
            statement.executeUpdate();
            fis.close();
            tempImage.delete();

            ResultSet rs = statement.getGeneratedKeys();

            return  rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            System.out.println("an error occurred" + e);
        }
        return  0;
    }

}
