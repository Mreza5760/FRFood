package org.FRFood.DAO;

import org.FRFood.entity.Order;

import java.sql.SQLException;

public interface OrderDAO {
    int insert(Order order) throws SQLException;
}