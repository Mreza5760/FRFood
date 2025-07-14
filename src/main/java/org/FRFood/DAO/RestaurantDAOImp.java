package org.FRFood.DAO;

import org.FRFood.util.Role;
import org.FRFood.entity.Food;
import org.FRFood.entity.Menu;
import org.FRFood.entity.User;
import org.FRFood.util.DBConnector;
import org.FRFood.entity.Restaurant;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class RestaurantDAOImp implements RestaurantDAO {
    @Override
    public int insert(Restaurant restaurant, int userId) throws SQLException, DataAlreadyExistsException {
        String sql = "INSERT INTO restaurants (owner_id , name , address , phone , logo , tax_fee , additional_fee) VALUES (?,?,?,?,?,?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, restaurant.getName());
            preparedStatement.setString(3, restaurant.getAddress());
            preparedStatement.setString(4, restaurant.getPhone());
            preparedStatement.setString(5, restaurant.getLogo());
            preparedStatement.setInt(6, restaurant.getTaxFee());
            preparedStatement.setInt(7, restaurant.getAdditionalFee());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID generated.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    @Override
    public Optional<Restaurant> getById(int id) throws SQLException {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setId(rs.getInt("id"));
                    restaurant.setName(rs.getString("name"));
                    restaurant.setAddress(rs.getString("address"));
                    restaurant.setPhone(rs.getString("phone"));
                    restaurant.setLogo(rs.getString("logo"));
                    restaurant.setTaxFee(rs.getInt("tax_fee"));
                    restaurant.setAdditionalFee(rs.getInt("additional_fee"));
                    return Optional.of(restaurant);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public List<Restaurant> searchByString(String search) throws SQLException {
        String sql = "SELECT * FROM restaurants WHERE name LIKE ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, "%" + search + "%");
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<Restaurant> restaurants = new ArrayList<>();
                while (rs.next()) {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setId(rs.getInt("id"));
                    restaurant.setName(rs.getString("name"));
                    restaurant.setAddress(rs.getString("address"));
                    restaurant.setPhone(rs.getString("phone"));
                    restaurant.setLogo(rs.getString("logo"));
                    restaurant.setTaxFee(rs.getInt("tax_fee"));
                    restaurant.setAdditionalFee(rs.getInt("additional_fee"));
                    restaurants.add(restaurant);
                }
                return restaurants;
            }
        }
    }

    @Override
    public void DeleteById(int id) throws SQLException {
        String sql = "DELETE FROM restaurants WHERE id = ?";
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
    public List<Food> getFoods(int id) throws SQLException {
        String sql = "SELECT * FROM fooditems WHERE restaurant_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<Food> foods = new ArrayList<>();
                while (rs.next()) {
                    Food food = new Food();
                    food.setId(rs.getInt("id"));
                    food.setPrice(rs.getInt("price"));
                    food.setName(rs.getString("name"));
                    food.setSupply(rs.getInt("supply"));
                    food.setMenuID(rs.getInt("menu_id"));
                    food.setPicture(rs.getString("image"));
                    food.setRestaurantId(rs.getInt("restaurant_id"));
                    food.setDescription(rs.getString("description"));

                    FoodDAO foodDAO = new FoodDAOImp();
                    food.setKeywords(foodDAO.getKeywords(food.getId()));

                    foods.add(food);
                }
                return foods;
            }
        }
    }

    @Override
    public void Update(Restaurant restaurant) throws SQLException {
        String sql = "INSERT INTO restaurants (id,owner_id , name , address , phone , logo , tax_fee , additional_fee) VALUES (?,?,?,?,?,?,?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, restaurant.getId());
            preparedStatement.setInt(2, restaurant.getOwner().getId());
            preparedStatement.setString(3, restaurant.getName());
            preparedStatement.setString(4, restaurant.getAddress());
            preparedStatement.setString(5, restaurant.getPhone());
            preparedStatement.setString(6, restaurant.getLogo());
            preparedStatement.setInt(7, restaurant.getTaxFee());
            preparedStatement.setInt(8, restaurant.getAdditionalFee());

            DeleteById(restaurant.getId());
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
        }
    }

    @Override
    public List<Menu> getMenus(int id) throws SQLException {
        String sql = "SELECT * FROM menus WHERE restaurant_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<Menu> menus = new ArrayList<>();
                while (rs.next()) {
                    Menu menu = new Menu();
                    menu.setId(rs.getInt("id"));
                    menu.setTitle(rs.getString("title"));
                    RestaurantDAO restaurantDAO = new RestaurantDAOImp();
                    Optional<Restaurant> restaurant = restaurantDAO.getById(rs.getInt("restaurant_id"));
                    menu.setRestaurant(restaurant.orElse(null));
                    menus.add(menu);
                }
                return menus;
            }
        }
    }

    @Override
    public int insertMenu(Menu menu) throws SQLException {
        return 0;
    }

    @Override
    public void deleteMenuByTitle(String title, int restaurantId) throws SQLException {

    }

    @Override
    public Optional<Menu> getMenuByTitle(String title, int restaurantId) throws SQLException {
        return Optional.empty();
    }
}