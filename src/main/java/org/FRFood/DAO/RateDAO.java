package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Rate;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface RateDAO {
    int insert(Rate rate) throws SQLException;
    void deleteById(int id) throws SQLException;
    void updateById(int id, Rate rate) throws SQLException;
    Optional<Rate> getById(int id) throws SQLException;
    List<Rate> getFoodRates(Food food) throws SQLException;
}