package org.FRFood.DAO;

import org.FRFood.entity.Order;
import org.FRFood.entity.OrderItem;
import org.FRFood.util.DBConnector;
import org.FRFood.util.Status;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImp implements OrderDAO {
    @Override
    public int insert(Order order) throws SQLException {
        String sqlOrder = """
            INSERT INTO Orders (customer_id, restaurant_id, courier_id, coupon_id,
                                 delivery_address, raw_price, tax_fee, additional_fee,
                                 courier_fee, pay_price, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnector.gConnection()) {
            conn.setAutoCommit(false); // Transaction start
            int orderId;

            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getCustomerId());
                stmt.setInt(2, order.getRestaurantId());
                if (order.getCourierId() != null) stmt.setInt(3, order.getCourierId()); else stmt.setNull(3, Types.INTEGER);
                if (order.getCouponId() != null) stmt.setInt(4, order.getCouponId()); else stmt.setNull(4, Types.INTEGER);
                stmt.setString(5, order.getDeliveryAddress());
                stmt.setInt(6, order.getRawPrice());
                stmt.setInt(7, order.getTaxFee());
                stmt.setInt(8, order.getAdditionalFee());
                stmt.setInt(9, order.getCourierFee());
                stmt.setInt(10, order.getPayPrice());
                stmt.setString(11, order.getStatus().name());

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    throw new SQLException("Insert failed, no rows affected.");
                }

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    } else {
                        conn.rollback();
                        throw new SQLException("Insert failed, no ID generated.");
                    }
                }
            }

            insertOrderItems(conn, orderId, order.getItems());
            conn.commit(); // All good

            return orderId;
        }
    }

    @Override
    public Optional<Order> getById(Integer id) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE id = ?";
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(loadOrderItems(conn, id));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Order> getUserOrders(int userID) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE customer_id = ? ORDER BY created_at DESC";
        return fetchOrders(sql, userID);
    }

    @Override
    public List<Order> getAvailableOrders() throws SQLException {
        String sql = "SELECT * FROM Orders WHERE status = 'finding courier' ORDER BY created_at ASC";
        return fetchOrders(sql);
    }

    @Override
    public List<Order> getCourierOrders(int courierID) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE courier_id = ? ORDER BY created_at DESC";
        return fetchOrders(sql, courierID);
    }

    @Override
    public List<Order> getRestaurantOrders(int restaurantID) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE restaurant_id = ? ORDER BY created_at DESC";
        return fetchOrders(sql, restaurantID);
    }

    @Override
    public void changeStatus(Integer orderID, Status status) throws SQLException {
        String sql = "UPDATE Orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderID);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No order updated (id=" + orderID + ")");
            }
        }
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT * FROM Orders ORDER BY created_at DESC";
        return fetchOrders(sql);
    }

    /* ---------------- Private Helpers ---------------- */

    private List<Order> fetchOrders(String sql, Object... params) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(loadOrderItems(conn, order.getId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    private void insertOrderItems(Connection conn, int orderId, List<OrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) return;

        String sql = "INSERT INTO Order_Items (order_id, item_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getItemId());
                stmt.setInt(3, item.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private List<OrderItem> loadOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT item_id, quantity FROM Order_Items WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setRestaurantId(rs.getInt("restaurant_id"));
        order.setCourierId((Integer) rs.getObject("courier_id"));
        order.setCouponId((Integer) rs.getObject("coupon_id"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setRawPrice(rs.getInt("raw_price"));
        order.setTaxFee(rs.getInt("tax_fee"));
        order.setAdditionalFee(rs.getInt("additional_fee"));
        order.setCourierFee(rs.getInt("courier_fee"));
        order.setPayPrice(rs.getInt("pay_price"));

        // Normalize status enum (handles spaces like "unpaid and cancelled")
        String statusStr = rs.getString("status").replace(" ", "_").toUpperCase();
        order.setStatus(Status.valueOf(statusStr));

        order.setCreatedAt(rs.getString("created_at"));
        order.setUpdatedAt(rs.getString("updated_at"));
        return order;
    }
}