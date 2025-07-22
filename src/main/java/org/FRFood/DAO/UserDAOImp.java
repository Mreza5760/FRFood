package org.FRFood.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.FRFood.util.Role;
import org.FRFood.entity.User;
import org.FRFood.util.DBConnector;
import org.FRFood.entity.Restaurant;
import org.FRFood.entity.BankAccount;

public class UserDAOImp implements UserDAO {
    @Override
    public int insert(User user) throws SQLException {
        BankAccountDAO bankAccountDAO = new BankAccountDAOImp();
        String sql = "INSERT INTO Users (full_name, phone, wallet, email, password_hash, role, address, profile_image, bank_id, confirmed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhoneNumber());
            stmt.setInt(3, 0);
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPassword());
            stmt.setString(6, user.getRole().toString());
            stmt.setString(7, user.getAddress());
            stmt.setString(8, user.getPicture());

            if (user.getBank() != null) {
                int bankId = bankAccountDAO.insert(user.getBank());
                stmt.setInt(9, bankId);
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setBoolean(10, user.getRole() == Role.buyer);


            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID generated.");
                }
            }
        }
    }

    @Override
    public void insertFavorite(int id, Restaurant restaurant) throws SQLException {
        String sql =  "INSERT INTO favorite_restaurants (user_id, restaurant_id) VALUES (?, ?)";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
                ){
            stmt.setInt(1, id);
            stmt.setInt(2, restaurant.getId());
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
        }
    }

    @Override
    public void deleteFavorite(int id, Restaurant restaurant) throws SQLException {
        String sql = "DELETE FROM favorite_restaurants WHERE user_id = ? AND restaurant_id = ?";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
                ){
            stmt.setInt(1, id);
            stmt.setInt(2, restaurant.getId());
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }
        }
    }

    @Override
    public void setWallet(int userId, int amount) throws SQLException {
        String sql = "UPDATE Users SET wallet = ? WHERE id = ?";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
                ){
            stmt.setInt(1, amount);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }
        }
    }

    @Override
    public List<User> getAllUser() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ){
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    User user;
                    user = getById(rs.getInt("id")).orElse(null);
                    users.add(user);
                }
            }
        }
        return users;
    }

    @Override
    public Optional<User> getById(int id) throws SQLException{
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setPhoneNumber(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setRole(Role.valueOf(rs.getString("role")));
                    user.setAddress(rs.getString("address"));
                    user.setPicture(rs.getString("profile_image"));
                    user.setConfirmed(rs.getBoolean("confirmed"));
                    user.setWallet(rs.getInt("wallet"));

                    int bankId = rs.getInt("bank_id");
                    if (bankId != 0) {
                        BankAccountDAO bankDAO = new BankAccountDAOImp();
                        user.setBankAccount(bankDAO.getById(bankId).orElse(null));
                    }
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Restaurant> getFavorites(int id) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT * FROM favorite_restaurants WHERE user_id = ?";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
                ){
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setId(rs.getInt("restaurant_id"));
                    RestaurantDAO restaurantDAO = new RestaurantDAOImp();
                    restaurant = restaurantDAO.getById(restaurant.getId()).orElse(null);
                    restaurants.add(restaurant);
                }
            }
        }
        return restaurants;
    }

    @Override
    public boolean deleteById(int id) throws  SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<User> getByPhone(String phoneNumber) throws SQLException {
        String sql = "SELECT * FROM Users WHERE phone = ?";
        User user = null;
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = getById(rs.getInt("id")).orElse(null);
                }
            }
        }
        return Optional.ofNullable(user);
    }

    @Override
    public void update(User currentUser) throws SQLException {
        String temp = "UPDATE Users SET full_name = ? , email = ? , password_hash = ? , address = ? , profile_image = ? , bank_id = ? WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(temp)
        ) {
            stmt.setString(1, currentUser.getFullName());
            stmt.setString(2, currentUser.getEmail());
            stmt.setString(3, currentUser.getPassword());
            stmt.setString(4, currentUser.getAddress());
            stmt.setString(5, currentUser.getPicture());
            stmt.setInt(6, currentUser.getBank().getId());
            stmt.setInt(7, currentUser.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void makeConfirmed(int id) throws SQLException {
        String sql = "UPDATE Users SET confirmed = true WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ){
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("no rows affected");
            }
        }
    }
}