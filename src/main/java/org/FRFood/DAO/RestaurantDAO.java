package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Menu;
import org.FRFood.entity.Restaurant;
import org.FRFood.util.DataAlreadyExistsException;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface RestaurantDAO {
    void DeleteById(int id) throws SQLException;
    List<Food> getFoods(int id) throws SQLException;
    List<Menu> getMenus(int id) throws SQLException;
    Optional<Restaurant> getById(int id) throws SQLException;
    void Update(Restaurant restaurant) throws SQLException;
    List<Restaurant> searchByString(String search) throws SQLException;
    int insert(Restaurant restaurant,int userId) throws SQLException, DataAlreadyExistsException;
    int insertMenu(Menu menu) throws SQLException;
}