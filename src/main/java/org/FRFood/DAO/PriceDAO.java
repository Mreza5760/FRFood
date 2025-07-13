package org.FRFood.DAO;

import org.FRFood.entity.Price;
import org.FRFood.util.DataAlreadyExistsException;

import java.util.Optional;
import java.sql.SQLException;

public interface PriceDAO {
    Optional<Price> getById(int id) throws SQLException;
//    int insert(Price price) throws SQLException, DataAlreadyExistsException;
//    boolean deleteById(int id) throws SQLException;
//    void update(Price price) throws SQLException;
}
