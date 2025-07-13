package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface FoodDAO{
    boolean doesHaveKeyword(List<String> input) throws SQLException;
}