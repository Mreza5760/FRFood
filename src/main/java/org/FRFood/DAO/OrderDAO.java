package org.FRFood.DAO;

import org.FRFood.entity.Order;

import java.util.Optional;
import java.sql.SQLException;

public interface OrderDAO {
    int insert(Order order) throws SQLException;
    Optional<Order> getById(Integer id) throws SQLException;
}