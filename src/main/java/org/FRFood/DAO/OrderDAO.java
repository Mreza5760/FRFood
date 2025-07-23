package org.FRFood.DAO;

import org.FRFood.entity.Order;
import org.FRFood.entity.Status;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface OrderDAO {
    int insert(Order order) throws SQLException;

    Optional<Order> getById(Integer id) throws SQLException;

    List<Order> getUserOrders(int userID) throws SQLException;

    List<Order> getAvailableOrders() throws SQLException;

    List<Order> getCourierOrders(int courierID) throws SQLException;

    List<Order> getRestaurantOrders(int restaurantID) throws SQLException;

    void changeStatus(Integer orderID, Status status,int userId) throws SQLException;

    List<Order> getAllOrders() throws SQLException;
}