package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Menu;
import org.FRFood.entity.User;
import org.FRFood.util.DBConnector;
import org.FRFood.entity.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class RestaurantDAOImp implements RestaurantDAO {
    @Override
    public int insert(Restaurant restaurant, int userId) throws SQLException {
        String sql = "INSERT INTO restaurants (owner_id , name , address , phone , logo , tax_fee , additional_fee) VALUES (?,?,?,?,?,?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, restaurant.getName());
            preparedStatement.setString(3, restaurant.getAddress());
            preparedStatement.setString(4, restaurant.getPhone());
            if (restaurant.getLogo() != null && !restaurant.getLogo().isEmpty()) {
                preparedStatement.setString(5, restaurant.getLogo());
            } else {
                byte[] fileContent = Files.readAllBytes(Paths.get("src/main/resources/imageUrls/restaurant.png"));
                String base64String = Base64.getEncoder().encodeToString(fileContent);
                preparedStatement.setString(5, base64String);
            }
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
        }
         catch (IOException e) {
            throw new SQLException(e);
        }
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
                    int ownerId = rs.getInt("owner_id");
                    UserDAO userDAO = new UserDAOImp();
                    restaurant.setOwner(userDAO.getById(ownerId).orElse(null));
                    return Optional.of(restaurant);
                }
            }
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
                    int ownerId = rs.getInt("owner_id");
                    UserDAO userDAO = new UserDAOImp();
                    restaurant.setOwner(userDAO.getById(ownerId).orElse(null));
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
                    FoodDAO foodDAO = new FoodDAOImp();
                    foods.add(foodDAO.getById(rs.getInt("id")).orElse(null));
                }
                return foods;
            }
        }
    }

    @Override
    public List<Food> getMenuFood(int restaurantId, int menuId) throws SQLException {
        List<Food> foods = new ArrayList<>();
        String sql = "SELECT * FROM fooditem_menus WHERE menu_id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            preparedStatement.setInt(1, menuId);
            try(ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    FoodDAO foodDAO = new FoodDAOImp();
                    foods.add(foodDAO.getById(rs.getInt("food_item_id")).orElse(null));
                }
            }
        }
        return foods;
    }

    @Override
    public void Update(Restaurant restaurant) throws SQLException {
        String sql = "UPDATE restaurants SET name=? ,address=?, phone=? , logo=? , tax_fee=? , additional_fee=? WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(7, restaurant.getId());
            preparedStatement.setString(1, restaurant.getName());
            preparedStatement.setString(2, restaurant.getAddress());
            preparedStatement.setString(3, restaurant.getPhone());
            if (restaurant.getLogo() != null && !restaurant.getLogo().isEmpty()) {
                preparedStatement.setString(4, restaurant.getLogo());
            } else {
                byte[] fileContent = Files.readAllBytes(Paths.get("src/main/resources/imageUrls/restaurant.png"));
                String base64String = Base64.getEncoder().encodeToString(fileContent);
                preparedStatement.setString(4, base64String);
            }
            preparedStatement.setInt(5, restaurant.getTaxFee());
            preparedStatement.setInt(6, restaurant.getAdditionalFee());
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
        }catch (Exception e){
            throw new SQLException(e.getMessage());
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
        int generatedKey = -1;
        String sql = "INSERT INTO menus (restaurant_id,title) VALUES (?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setInt(1, menu.getRestaurant().getId());
            preparedStatement.setString(2, menu.getTitle());
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    menu.setId(generatedKeys.getInt(1));
                    generatedKey = menu.getId();
                }
            }
        }
        return generatedKey;
    }

    @Override
    public void deleteMenuByTitle(String title, int restaurantId) throws SQLException {
        String sql = "DELETE FROM menus WHERE restaurant_id = ? AND title = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, restaurantId);
            preparedStatement.setString(2, title);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }
        }
    }

    @Override
    public Optional<Menu> getMenuByTitle(String title, int restaurantId) throws SQLException {
        String sql = "SELECT * FROM menus WHERE restaurant_id = ? AND title = ?";
        Menu menu = null;
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, restaurantId);
            preparedStatement.setString(2, title);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                menu = new Menu();
                menu.setId(rs.getInt("id"));
                menu.setTitle(rs.getString("title"));
                menu.setRestaurant(getById(rs.getInt("restaurant_id")).orElse(null));
            }
        }
        return Optional.of(menu);
    }

    @Override
    public List<Restaurant> getByOwnerId(int ownerId) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT * FROM Restaurants WHERE owner_id = ?";
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Restaurant restaurant = new Restaurant(new User(),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("logo"),
                        rs.getInt("tax_fee"),
                        rs.getInt("additional_fee"));
                restaurant.setId(rs.getInt("id"));
                restaurants.add(restaurant);
            }

        }
        return restaurants;
    }
}