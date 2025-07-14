package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.util.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FoodDAOImp implements FoodDAO{

    @Override
    public Optional<Food> getById(int id) {
        return Optional.empty();
    }

    @Override
    public boolean doesHaveKeywords(List<String> input) throws SQLException {
        return false;
    }

    @Override
    public int insert(Food food) throws SQLException{
        return 0;
    }

    @Override
    public void update(Food food) throws SQLException {
        return;
    }

    @Override
    public void delete(Food food) throws SQLException {
        return;
    }
}