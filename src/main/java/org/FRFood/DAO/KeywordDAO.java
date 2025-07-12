package org.FRFood.DAO;

import org.FRFood.entity.Keyword;
import org.FRFood.util.DataAlreadyExistsException;

import java.util.Optional;
import java.sql.SQLException;

public interface KeywordDAO {
    int insertKeyword(Keyword keyword)throws SQLException , DataAlreadyExistsException;
    Optional<Keyword> getKeywordById(int id) throws SQLException;
    Optional<Keyword> getKeywordByName(String Name)throws SQLException;
}