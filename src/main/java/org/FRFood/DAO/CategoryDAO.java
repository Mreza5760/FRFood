package org.FRFood.DAO;

import org.FRFood.entity.Category;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.SQLException;
import java.util.Optional;

public interface CategoryDAO {
    int insertCategory(String name)throws SQLException , DataAlreadyExistsException;
    Optional<Category> getCategoryById(int id) throws SQLException;
    Optional<Category> getCategoryByName(String Name)throws SQLException;
}
