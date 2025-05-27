package org.FRFood.DAO;

import org.FRFood.entity.BankAccount;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.SQLException;
import java.util.Optional;

public interface BankAccountDAO {
    Optional<BankAccount> getBankAccountById(int id) throws SQLException;
    Optional<BankAccount> getBankAccountByAccountNumber(String accountNumber) throws SQLException   ;
    int insertBankAccount(BankAccount bankAccount) throws SQLException , DataAlreadyExistsException;
}
