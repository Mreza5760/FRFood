package org.FRFood.DAO;

import org.FRFood.entity.Food;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface FoodDAO {
    Optional<Food> getById(int id) throws SQLException;
    boolean doesHaveKeywords(List<String> input) throws SQLException;
}