package org.FRFood.DAO;

import org.FRFood.entity.Transaction;
import org.FRFood.util.DBConnector;
import org.FRFood.entity.TransactionMethod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImp implements TransactionDAO {

    @Override
    public List<Transaction> getUserTransactions(int userId) throws SQLException {
        String sql = "SELECT * FROM Transactions WHERE user_id = ?";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> getAllTransactions() throws SQLException {
        String sql = "SELECT * FROM Transactions";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnector.gConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        }
        return transactions;
    }

    @Override
    public int insert(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (order_id, user_id, method, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (transaction.getOrderID() != 0)
                stmt.setInt(1, transaction.getOrderID());
            else
                stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, transaction.getUserID());
            stmt.setString(3, transaction.getMethod().name());
            stmt.setInt(4, transaction.getAmount());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Inserting transaction failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Inserting transaction failed, no ID obtained.");
                }
            }
        }
    }

    // Helper to map a ResultSet row to Transaction object
    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        int orderId = rs.getInt("order_id");
        transaction.setOrderID(rs.wasNull() ? 0 : orderId);
        transaction.setUserID(rs.getInt("user_id"));
        transaction.setMethod(TransactionMethod.valueOf(rs.getString("method")));
        transaction.setAmount(rs.getInt("amount"));
        return transaction;
    }
}