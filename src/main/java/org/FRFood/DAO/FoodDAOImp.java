package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.util.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;

public class FoodDAOImp implements FoodDAO{
    @Override
    public int insertFood(Food food) {
        String sql = "INSERT INTO FoodItems (vendor_id, name, image, count, price_id, picture_id) VALUES (? , ? , ? , ? )";

        try(
                Connection connection = DatabaseConnector.gConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
        )

        {
            statement.setString(1, food.getName());
            statement.setString(2, food.getDescription());
            statement.setString(5, food.getPicture());

            statement.executeUpdate();

        }catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    public Optional<Food> getFoodById(int id) {
        return Optional.empty();
    }
}
