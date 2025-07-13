package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.util.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FoodDAOImp implements FoodDAO{
//    @Override
    public int insert(Food food) {
        String sql = "INSERT INTO FoodItems (vendor_id, name, image, count, price_id, picture_id) VALUES (? , ? , ? , ? )";

        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
        )

        {
            statement.setString(1, food.getName());
            statement.setString(2, food.getDescription());
            statement.setString(5, food.getPicture());

            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println(e);
        }
        return 0;
    }

//    @Override
    public Optional<Food> getById(int id) {
        return Optional.empty();
    }

    @Override
    public boolean doesHaveKeyword(List<String> input) throws SQLException {
        return false;
    }
}