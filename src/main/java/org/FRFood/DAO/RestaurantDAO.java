package org.FRFood.DAO;

import org.FRFood.entity.Restaurant;
import org.FRFood.entity.User;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.SQLException;
import java.util.Optional;

public interface RestaurantDAO {
    int insert(Restaurant restaurant,int userId) throws SQLException, DataAlreadyExistsException;
    Optional<Restaurant> getById(int id) throws SQLException;
}
