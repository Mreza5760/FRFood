package org.FRFood.DAO;

import org.FRFood.entity.BankAccount;
import org.FRFood.util.DataAlreadyExistsException;

import java.util.Optional;
import java.sql.SQLException;

public interface BankAccountDAO {
    Optional<BankAccount> getById(int id) throws SQLException;

    int insert(BankAccount bankAccount) throws SQLException, DataAlreadyExistsException;

    boolean deleteById(int id) throws SQLException;

    void update(BankAccount bankAccount) throws SQLException;
}
