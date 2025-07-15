package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Rate;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public class RateDAOImp implements RateDAO {
    @Override
    public int insert(Rate rate) throws SQLException {
        return 0;
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        return false;
    }

    @Override
    public boolean updateById(int id, Rate rate) throws SQLException {
        return false;
    }

    @Override
    public Optional<Rate> getById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Rate> getFoodRates(Food food) throws SQLException {
        return List.of();
    }
}