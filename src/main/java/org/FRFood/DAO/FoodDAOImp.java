package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Keyword;
import org.FRFood.entity.Restaurant;
import org.FRFood.util.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FoodDAOImp implements FoodDAO {

    @Override
    public Optional<Food> getById(int id) {
        String sql = "SELECT * FROM FoodItems WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Food food = new Food();
                    food.setId(rs.getInt("id"));
                    food.setName(rs.getString("name"));
                    food.setDescription(rs.getString("description"));
                    food.setPrice(rs.getInt("price"));
                    food.setRestaurantId(rs.getInt("restaurant_id"));
                    food.setPicture(rs.getString("image"));
                    food.setSupply(rs.getInt("supply"));
                    KeywordDAO  keywordDAO = new KeywordDAOImp();
                    List<Keyword> keywords= keywordDAO.getKeywordsByFoodId(food.getId());
                    food.setKeywords(keywords);
                    return Optional.of(food);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public boolean doesHaveKeywords(List<String> input) throws SQLException {
        return false;
    }

    @Override
    public int insert(Food food) throws SQLException {
        return 0;
    }

    @Override
    public void update(Food food) throws SQLException {
        return;
    }

    @Override
    public void delete(int id) throws SQLException {
        return;
    }

    @Override
    public void setMenuId(int menuId, int foodId) throws SQLException {

    }

    @Override
    public void setMenuIdNull(int foodId) throws SQLException {

    }
}