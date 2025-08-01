package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Menu;
import org.FRFood.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface RestaurantDAO {
    void DeleteById(int id) throws SQLException;

    List<Food> getFoods(int id) throws SQLException;

    List<Food> getMenuFood(int restaurantId, int menuId) throws SQLException;

    List<Menu> getMenus(int id) throws SQLException;

    Optional<Restaurant> getById(int id) throws SQLException;

    void Update(Restaurant restaurant) throws SQLException;

    List<Restaurant> searchByString(String search) throws SQLException;

    int insert(Restaurant restaurant, int userId) throws SQLException;

    int insertMenu(Menu menu) throws SQLException;

    void deleteMenuByTitle(String title, int restaurantId) throws SQLException;

    Optional<Menu> getMenuByTitle(String title, int restaurantId) throws SQLException;

    List<Restaurant> getByOwnerId(int ownerId) throws SQLException;
}