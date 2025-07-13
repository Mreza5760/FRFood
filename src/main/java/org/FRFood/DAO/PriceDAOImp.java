package org.FRFood.DAO;

import org.FRFood.entity.Price;

import java.sql.SQLException;
import java.util.Optional;

public class PriceDAOImp implements PriceDAO {
    @Override
    public Optional<Price> getById(int id) throws SQLException {
        return Optional.empty();
    }
}
