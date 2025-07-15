package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Keyword;
import org.FRFood.util.DBConnector;

import java.sql.*;
import java.util.*;

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
    public boolean doesHaveKeywords(List<String> input, int foodId) throws SQLException {
        Food food = getById(foodId).orElse(null);
        if(food==null){
            throw new SQLException("Food Not Found");
        }
        Set<String> foodKeywordNames = new HashSet<String>();
        for(Keyword keyword : food.getKeywords()){
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
            preparedStatement.setString(3, food.getPicture());
            preparedStatement.setString(4, food.getDescription());
            preparedStatement.setInt(5, food.getPrice());
            preparedStatement.setInt(6, food.getSupply());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedKey =  keys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID generated.");
                }
            }

            food.setId(generatedKey);
            List<Keyword> keywords = food.getKeywords();
            String sql2 = "INSERT INTO FoodItem_Keywords (food_item_id,keyword_id) VALUES (?,?)";
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            for(Keyword keyword:keywords){
                preparedStatement2.setInt(1, food.getId());
                preparedStatement2.setInt(2, keyword.getId());
                int changed = preparedStatement2.executeUpdate();
                if(changed == 0){
                    throw  new SQLException();
                }
            }
            preparedStatement2.close();

        }
        return generatedKey;
    }

    @Override
    public void update(Food food) throws SQLException {
        delete(food.getId());
        String sql = "INSERT INTO FoodItems (id , restaurant_id , name , image , description , price , supply) VALUES (?,?,?,?,?,?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, food.getId());
            preparedStatement.setInt(2, food.getRestaurantId());
            preparedStatement.setString(3, food.getName());
            preparedStatement.setString(4, food.getPicture());
            preparedStatement.setString(5, food.getDescription());
            preparedStatement.setInt(6, food.getPrice());
            preparedStatement.setInt(7, food.getSupply());

            List<Keyword> keywords = food.getKeywords();
            String sql2 = "INSERT INTO FoodItem_Keywords (food_item_id,keyword_id) VALUES (?,?)";
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            for(Keyword keyword:keywords){
                preparedStatement2.setInt(1, food.getId());
                preparedStatement2.setInt(2, keyword.getId());
                int changed = preparedStatement2.executeUpdate();
                if(changed == 0){
                    throw  new SQLException();
                }
            }
            preparedStatement2.close();
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM FoodItems WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            preparedStatement.setInt(1, id);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }
        }
    }

    @Override
    public void setMenuId(int menuId, int foodId) throws SQLException {
        String sql = "UPDATE FoodItems SET menu_id = ? WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ){
            preparedStatement.setInt(1, menuId);
            preparedStatement.setInt(2, foodId);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }
        }
    }

    @Override
    public void setMenuIdNull(int foodId) throws SQLException {
        String sql = "UPDATE FoodItems SET menu_id = null WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ){
            preparedStatement.setInt(1, foodId);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }
        }
    }

    @Override
    public List<Keyword> getKeywords(int foodId) throws SQLException {
        List<Keyword> keywords = new ArrayList<>();
        String sql = "SELECT * FROM FoodItem_Keywords WHERE food_item_id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ){
            preparedStatement.setInt(1, foodId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("keyword_id");
                KeywordDAO keywordDAO = new KeywordDAOImp();
                if(keywordDAO.getKeywordById(id).isEmpty()){
                    keywords.add(keywordDAO.getKeywordById(id).get());
                }else{
                    throw new SQLException();
                }
            }
            resultSet.close();
            return keywords;
        }
    }
}