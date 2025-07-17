package org.FRFood.DAO;

import org.FRFood.DTO.OrderInputDTO;
import org.FRFood.entity.Order;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface OrderDAO {
    int insert(OrderInputDTO order) throws SQLException;

    Optional<Order> getById(Integer id) throws SQLException;

    List<Order> getUserOrders(int userID) throws SQLException;

    List<Order> getAvailableOrders() throws SQLException;

    List<Order> getRestaurantOrders(int restaurantID) throws SQLException;

    void changeStatus(Integer orderID, String status) throws SQLException;
}