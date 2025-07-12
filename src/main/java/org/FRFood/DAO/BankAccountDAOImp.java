package org.FRFood.DAO;

import org.FRFood.entity.BankAccount;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.DatabaseConnector;

import java.sql.*;
import java.util.Optional;

public class BankAccountDAOImp implements BankAccountDAO {

    @Override
    public int insert(BankAccount bankAccount) throws SQLException {
        String sql = "INSERT INTO bank_account (bank_name, account_number) VALUES (?, ?)";
        try (
                Connection conn = DatabaseConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, bankAccount.getName());
            stmt.setString(2, bankAccount.getAccountNumber());

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID generated.");
                }
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                throw new DataAlreadyExistsException("Bank account already exists.");
            }
            throw e;
        }
    }

    @Override
    public Optional<BankAccount> getById(int id) throws SQLException {
        String sql = "SELECT id, bank_name, account_number FROM bank_account WHERE id = ?";
        try (
                Connection conn = DatabaseConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BankAccount account = new BankAccount(rs.getString("bank_name"), rs.getString("account_number"));
                    account.setId(rs.getInt("id"));
                    return Optional.of(account);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM bank_account WHERE id = ?";
        try (
                Connection conn = DatabaseConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public void update(BankAccount bankAccount) throws SQLException {
        String temp = "UPDATE Bank_account Set bank_name = ?, account_number = ? WHERE id = ?";
        try(
                Connection conn = DatabaseConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(temp)
                ){
            stmt.setString(1, bankAccount.getName());
            stmt.setString(2, bankAccount.getAccountNumber());
            stmt.setInt(3, bankAccount.getId());
            stmt.executeUpdate();
        }
    }
}