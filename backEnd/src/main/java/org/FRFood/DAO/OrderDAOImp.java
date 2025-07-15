package org.FRFood.DAO;

import org.FRFood.entity.Order;
import org.FRFood.util.DBConnector;
import org.FRFood.DTO.OrderItemDTO;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.Status;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImp implements OrderDAO {
    @Override
    public int insert(Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, restaurant_id, courier_id, coupon_id, delivery_address, raw_price, tax_fee, courier_fee, pay_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            stmt.setInt(10, order.getPayPrice());
            stmt.setString(11, order.getStatus().toString());

        }
        return 0;
    }

    @Override
    public Optional<Order> getById(Integer id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(id);
                    order.setTaxFee(rs.getInt("tax_fee"));
                    order.setPayPrice(rs.getInt("pay_price"));
                    order.setCouponId(rs.getInt("coupon_id"));
                    order.setRawPrice(rs.getInt("raw_price"));
                    order.setCourierId(rs.getInt("courier_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setCourierFee(rs.getInt("courier_fee"));
                    order.setCreatedAt(rs.getString("created_at"));
                    order.setUpdatedAt(rs.getString("updated_at"));
                    order.setRestaurantId(rs.getInt("restaurant_id"));
                    order.setAdditionalFee(rs.getInt("additional_fee"));
                    order.setStatus(Status.valueOf(rs.getString("status")));
                    order.setDeliveryAddress(rs.getString("delivery_address"));
                }
            }
        }
    }

    @Override
    public List<Order> getUserOrders(int userID) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    Order order = new Order();
                    order.setCustomerId(userID);
                    order.setId(rs.getInt("id"));
                    order.setTaxFee(rs.getInt("tax_fee"));
                    order.setPayPrice(rs.getInt("pay_price"));
                    order.setCouponId(rs.getInt("coupon_id"));
                    order.setRawPrice(rs.getInt("raw_price"));
                    order.setCourierId(rs.getInt("courier_id"));
                    order.setCourierFee(rs.getInt("courier_fee"));
                    order.setCreatedAt(rs.getString("created_at"));
                    order.setUpdatedAt(rs.getString("updated_at"));
                    order.setRestaurantId(rs.getInt("restaurant_id"));
                    order.setAdditionalFee(rs.getInt("additional_fee"));
                    order.setStatus(Status.valueOf(rs.getString("status")));
                    order.setDeliveryAddress(rs.getString("delivery_address"));
                }
            }
        }
    }

    @Override
    public List<Order> getRestaurantOrders(int restaurantID) throws SQLException {
        String sql = "SELECT * FROM orders WHERE restaurant_id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, restaurantID);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    Order order = new Order();
                    order.setRestaurantId((restaurantID);
                    order.setId(rs.getInt("id"));
                    order.setTaxFee(rs.getInt("tax_fee"));
                    order.setPayPrice(rs.getInt("pay_price"));
                    order.setCouponId(rs.getInt("coupon_id"));
                    order.setRawPrice(rs.getInt("raw_price"));
                    order.setCourierId(rs.getInt("courier_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setCourierFee(rs.getInt("courier_fee"));
                    order.setCreatedAt(rs.getString("created_at"));
                    order.setUpdatedAt(rs.getString("updated_at"));
                    order.setAdditionalFee(rs.getInt("additional_fee"));
                    order.setStatus(Status.valueOf(rs.getString("status")));
                    order.setDeliveryAddress(rs.getString("delivery_address"));
                }
            }
        }
    }

    @Override
    public void changeStatus(Integer orderID, String status) throws SQLException {

    }
}