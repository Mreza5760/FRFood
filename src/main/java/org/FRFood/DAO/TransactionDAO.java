package org.FRFood.DAO;

import org.FRFood.entity.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {
    List<Transaction> getUserTransactions(int userId) throws SQLException;

    List<Transaction> getAllTransactions() throws SQLException;

    int insert(Transaction transaction) throws SQLException;
}