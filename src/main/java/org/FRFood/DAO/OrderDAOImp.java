package org.FRFood.DAO;

import org.FRFood.entity.Order;
import org.FRFood.util.DBConnector;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class OrderDAOImp implements OrderDAO {
    @Override
    public int insert(Order order) throws SQLException {
        String sql = "INSERT INTO Orders";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
//
        } catch (SQLException e) {
//            if ("23000".equals(e.getSQLState())) {
//            }
//            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
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