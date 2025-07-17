package org.FRFood.DAO;

import java.sql.*;
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
        String sql = "INSERT INTO Users (full_name, phone, email, password_hash, role, address, profile_image, bank_id,confirmed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
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
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void insertFavorite(int id, Restaurant restaurant) throws SQLException {
    }

    @Override
    public void deleteFavorite(int id, Restaurant restaurant) throws SQLException {
    }

    @Override
    public void setWallet(int userId, int wallet) throws SQLException {

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
        return List.of();
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
        BankAccount bankAccount;
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
                    bankAccount = bankAccountDAO.getById(rs.getInt("bank_id")).get();
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
            stmt.executeUpdate();
        }
    }
}