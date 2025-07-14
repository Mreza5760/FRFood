package org.FRFood.DAO;

import org.FRFood.entity.Order;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public class OrderDAOImp implements OrderDAO {
    @Override
    public int insert(Order order) throws SQLException {
        return 0;
    }

    @Override
    public Optional<Order> getById(Integer id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Order> getUserOrders(int userID) throws SQLException {
        return List.of();
    }

    @Override
    public List<Order> getRestaurantOrders(int restaurantID) throws SQLException {
        return List.of();
    }

    @Override
    public void changeStatus(Integer orderID, String status) throws SQLException {
        return;
    }
}