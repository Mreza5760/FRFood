package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.util.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class foodDAO {
    public void insertFood(Food food) {
        String sql = "INSERT INTO Food (name, description, count, price_id, picture_id) VALUES (? , ? , ? , ? )";

        try(
        Connection connection = DatabaseConnector.gConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        )

        {
            statement.setString(1, food.getName());
            statement.setString(2, food.getDescription());
            statement.setInt(4, food.getPriceId());
            statement.setInt(5, food.getPictureId());

            statement.executeUpdate();

        }catch (Exception e){
            System.out.println(e);
        }
    }
}

