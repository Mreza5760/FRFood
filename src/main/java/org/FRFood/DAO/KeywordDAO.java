package org.FRFood.DAO;

import org.FRFood.entity.Keyword;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface KeywordDAO {
    int insertKeyword(Keyword keyword) throws SQLException;

    Optional<Keyword> getKeywordById(int id) throws SQLException;

    Optional<Keyword> getKeywordByName(String Name) throws SQLException;

    List<Keyword> getKeywordsByFoodId(int foodId) throws SQLException;

    List<Keyword> getAllKeywords() throws SQLException;
}