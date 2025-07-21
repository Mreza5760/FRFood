package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Keyword;
import org.FRFood.entity.Restaurant;
import org.FRFood.util.DBConnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class FoodDAOImp implements FoodDAO {

    @Override
    public Optional<Food> getById(int id) throws SQLException {
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
                    KeywordDAO keywordDAO = new KeywordDAOImp();
                    List<Keyword> keywords = keywordDAO.getKeywordsByFoodId(food.getId());
                    food.setKeywords(keywords);
                    return Optional.of(food);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean doesHaveKeywords(List<String> input, int foodId) throws SQLException {
        Food food = getById(foodId).orElse(null);
        if (food == null) {
            throw new SQLException("Food Not Found");
        }
        Set<String> foodKeywordNames = new HashSet<String>();
        for (Keyword keyword : food.getKeywords()) {
            foodKeywordNames.add(keyword.getName());
        }
        Set<String> inputSet = new HashSet<>(input);
        return foodKeywordNames.equals(inputSet);
    }

    @Override
    public int insert(Food food) throws SQLException {
        int generatedKey = -1;
        String sql = "INSERT INTO FoodItems ( restaurant_id , name , image , description , price , supply) VALUES (?,?,?,?,?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setInt(1, food.getRestaurantId());
            preparedStatement.setString(2, food.getName());
            if (food.getPicture() != null && !food.getPicture().isEmpty()) {
                preparedStatement.setString(3, food.getPicture());
            } else {
                byte[] fileContent = Files.readAllBytes(Paths.get("src/main/resources/imageUrls/chef-logo-design-illustration-restaurant-logo-vector.png"));
                String base64String = Base64.getEncoder().encodeToString(fileContent);
                preparedStatement.setString(3, base64String);
            }
            preparedStatement.setString(4, food.getDescription());
            preparedStatement.setInt(5, food.getPrice());
            preparedStatement.setInt(6, food.getSupply());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedKey = keys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID generated.");
                }
            }

            food.setId(generatedKey);
            List<Keyword> keywords = food.getKeywords();
            String sql2 = "INSERT INTO keywords (name,food_id) VALUES (?,?)";
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            for (Keyword keyword : keywords) {
                preparedStatement2.setString(1, keyword.getName());
                preparedStatement2.setInt(2, food.getId());
                int theRows = preparedStatement2.executeUpdate();
                if (theRows == 0) {
                    throw new SQLException("Insert failed, no rows affected.");
                }
            }
            preparedStatement2.close();
        } catch (IOException e) {
            throw new SQLException("Insert failed.", e);
        }
        return generatedKey;
    }

    @Override
    public void update(Food food) throws SQLException {
        String sql = "UPDATE FoodItems SET  restaurant_id=? , name=? , image=? , description=? , price=? , supply=?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, food.getRestaurantId());
            preparedStatement.setString(2, food.getName());
            preparedStatement.setString(3, food.getPicture());
            preparedStatement.setString(4, food.getDescription());
            preparedStatement.setInt(5, food.getPrice());
            preparedStatement.setInt(6, food.getSupply());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            String sql3 ="DELETE FROM keywords WHERE food_id = ?";
            try(
                    PreparedStatement stmt3 = connection.prepareStatement(sql3);
                    ){
                stmt3.setInt(1, food.getId());
                stmt3.executeUpdate();
            }

            List<Keyword> keywords = food.getKeywords();
            String sql2 = "INSERT INTO keywords (food_id,name) VALUES (?,?)";
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            for (Keyword keyword : keywords) {
                preparedStatement2.setInt(1, food.getId());
                preparedStatement2.setString(2, keyword.getName());
                int changed = preparedStatement2.executeUpdate();
                if (changed == 0) {
                    throw new SQLException();
                }
            }
            preparedStatement2.close();

        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM FoodItems WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }
        }
    }

    @Override
    public void addFoodToMenu(int menuId, int foodId) throws SQLException {
        String sql = "INSERT INTO FoodItem_Menus (menu_id , food_item_id) VALUES (? , ?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, menuId);
            preparedStatement.setInt(2, foodId);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }
        }
    }

    @Override
    public void deleteMenuItem(int menuId, int foodId) throws SQLException {
        String sql = "DELETE FROM fooditem_menus WHERE food_item_id = ? AND menu_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, foodId);
            preparedStatement.setInt(2, menuId);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }
        }
    }

    @Override
    public List<Keyword> getKeywords(int foodId) throws SQLException {
        List<Keyword> keywords = new ArrayList<>();
        String sql = "SELECT * FROM keywords WHERE food_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, foodId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name =  resultSet.getString("name");
                keywords.add(new Keyword(name));
            }
            resultSet.close();
            return keywords;
        }
    }

    @Override
    public List<Food> searchFood(String search) throws SQLException {
        String sql = "SELECT * FROM fooditems WHERE name LIKE ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, "%" + search + "%");
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<Food> foods = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Optional<Food> food = getById(id);
                    if (food.isEmpty())
                        throw new SQLException();
                    foods.add(food.get());
                }
                return foods;
            }
        }
    }
}