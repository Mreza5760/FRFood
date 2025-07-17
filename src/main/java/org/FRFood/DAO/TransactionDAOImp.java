package org.FRFood.DAO;

import org.FRFood.entity.Transaction;

import java.sql.SQLException;
import java.util.List;

public class TransactionDAOImp implements TransactionDAO {
    @Override
    public List<Transaction> getUserTransactions(int userId) throws SQLException {
        return List.of();
    }

    @Override
    public List<Transaction> getAllTransactions() throws SQLException {
        return List.of();
    }

    @Override
    public int insert(Transaction transaction) throws SQLException {
        return 0;
    }
}