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
        String sql = "INSERT INTO orders (customer_id, restaurant_id, courier_id, coupon_id, delivery_address, raw_price, tax_fee, additional_fee, courier_fee, discount_amount, pay_price, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setInt(1, order.getCustomerId());
            stmt.setInt(2, order.getRestaurantId());
            stmt.setInt(3, order.getCourierId());
            stmt.setInt(4, order.getCouponId());
            stmt.setString(5, order.getDeliveryAddress());
            stmt.setInt(6, order.getRawPrice());
            stmt.setInt(7, order.getTaxFee());
            stmt.setInt(8, order.getAdditionalFee());
            stmt.setInt(9, order.getCourierFee());
            stmt.setInt(10, );
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