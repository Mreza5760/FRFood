package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.SQLException;
import java.util.Optional;

public interface FoodDAO{
    int insertFood(Food food) throws DataAlreadyExistsException , SQLException;
    Optional<Food> getFoodById(int id);
}


