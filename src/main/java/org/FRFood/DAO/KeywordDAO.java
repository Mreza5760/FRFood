package org.FRFood.DAO;

import org.FRFood.entity.Keyword;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface KeywordDAO {
    List<Keyword> getKeywordsByFoodId(int foodId) throws SQLException;
}