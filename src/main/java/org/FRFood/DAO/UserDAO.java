package org.FRFood.DAO;

import org.FRFood.entity.User;
import org.FRFood.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface UserDAO {
    void deleteById(int id) throws SQLException;

    void update(User currentUser) throws SQLException;

    Optional<User> getById(int id) throws SQLException;

    List<Restaurant> getFavorites(int id) throws SQLException;

    Optional<User> getByPhone(String phoneNumber) throws SQLException;

    int insert(User user) throws SQLException;

    void insertFavorite(int id, Restaurant restaurant) throws SQLException;

    void deleteFavorite(int id, Restaurant restaurant) throws SQLException;

    void setWallet(int userId, int amount) throws SQLException;

    List<User> getAllUser() throws SQLException;

    void makeConfirmed(int id) throws SQLException;
}