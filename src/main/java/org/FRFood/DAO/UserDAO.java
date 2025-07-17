package org.FRFood.DAO;

import org.FRFood.entity.User;
import org.FRFood.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface UserDAO {
    boolean deleteById(int id) throws SQLException;

    void update(User currentUser) throws SQLException;

    Optional<User> getById(int id) throws SQLException;

    List<Restaurant> getFavorites(int id) throws SQLException;

    Optional<User> getByPhone(String phoneNumber) throws SQLException;

    int insert(User user) throws SQLException;

    void insertFavorite(int id, Restaurant restaurant) throws SQLException;

    void deleteFavorite(int id, Restaurant restaurant) throws SQLException;

    void setWallet(int userId, int wallet) throws SQLException;
}