package org.FRFood.DAO;

import java.util.List;
import java.sql.SQLException;

public interface FoodDAO{
    boolean doesHaveKeywords(List<String> input) throws SQLException;
}