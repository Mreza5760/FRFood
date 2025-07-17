package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Keyword;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface FoodDAO {
    Optional<Food> getById(int id) throws SQLException;

    boolean doesHaveKeywords(List<String> input,int foodId) throws SQLException;

    int insert(Food food) throws SQLException;

    void update(Food food) throws SQLException;

    void delete(int id) throws SQLException;

    void addFoodToMenu(int menuId, int foodId) throws SQLException;

    void deleteMenuItem(int menuId, int foodId) throws SQLException;

    List<Keyword> getKeywords(int foodId) throws SQLException;
}