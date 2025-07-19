package org.FRFood.DAO;

import org.FRFood.util.DBConnector;
import org.FRFood.entity.BankAccount;

import java.sql.*;
import java.util.Optional;

public class BankAccountDAOImp implements BankAccountDAO {
    @Override
    public int insert(BankAccount bankAccount) throws SQLException {
        String sql = "INSERT INTO bank_account (bank_name, account_number) VALUES (?, ?)";
        try (
                Connection conn = DBConnector.gConnection();
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
        }
    }

    @Override
    public Optional<BankAccount> getById(int id) throws SQLException {
        String sql = "SELECT bank_name, account_number FROM bank_account WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BankAccount account = new BankAccount(rs.getString("bank_name"), rs.getString("account_number"));
                    account.setId(id);
                    return Optional.of(account);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM bank_account WHERE id = ?";
        try (
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException("No row effected");
        }
    }

    @Override
    public void update(BankAccount bankAccount) throws SQLException {
        String temp = "UPDATE Bank_account Set bank_name = ?, account_number = ? WHERE id = ?";
        try(
                Connection conn = DBConnector.gConnection();
                PreparedStatement stmt = conn.prepareStatement(temp)
                ){
            stmt.setString(1, bankAccount.getName());
            stmt.setString(2, bankAccount.getAccountNumber());
            stmt.setInt(3, bankAccount.getId());
            int rows =stmt.executeUpdate();
            if(rows == 0){
                throw new SQLException("Update failed, no rows affected.");
            }
        }
    }
}