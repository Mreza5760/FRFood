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
        String sql = "INSERT INTO Users (full_name, phone, email, password_hash, role, address, profile_image, bank_id, confirmed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhoneNumber());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().toString());
            stmt.setString(6, user.getAddress());
            stmt.setString(7, user.getPicture());

            if (user.getBank() != null) {
                int bankId = bankAccountDAO.insert(user.getBank());
                stmt.setInt(8, bankId);
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setBoolean(9, user.getRole() == Role.buyer);


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
    public void setWallet(int userId, int wallet) throws SQLException {
        String sql = "UPDATE Users SET wallet = ? WHERE user_id = ?";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
                ){
            stmt.setInt(1, wallet);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
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

                    int bankId = rs.getInt("bank_id");
                    if (!rs.wasNull()) {
                        BankAccountDAO bankDAO = new BankAccountDAOImp();
                        user.setBankAccount(bankDAO.getById(bankId).orElse(null));
                    }

                    users.add(user);
                }
            }
        }
        return users;
    }

    @Override
    public Optional<User> getById(int id) {
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

                    int bankId = rs.getInt("bank_id");
                    if (!rs.wasNull()) {
                        BankAccountDAO bankDAO = new BankAccountDAOImp();
                        user.setBankAccount(bankDAO.getById(bankId).orElse(null));
                    }

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
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
    public boolean deleteById(int id) {
        String sql = "DELETE FROM Users WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getByPhone(String phoneNumber) {
        String sql = "SELECT * FROM Users WHERE phone = ?";
        BankAccountDAO bankAccountDAO = new BankAccountDAOImp();
        User user = null;
        BankAccount bankAccount = null;
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setPhoneNumber(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setRole(Role.valueOf(rs.getString("role")));
                    user.setAddress(rs.getString("address"));
                    user.setPicture(rs.getString("profile_image"));
                    bankAccount = bankAccountDAO.getById(rs.getInt("bank_id")).orElse(null);
                    user.setConfirmed(rs.getBoolean("confirmed"));
                    user.setBankAccount(bankAccount);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
            System.out.println(currentUser.getPicture());
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