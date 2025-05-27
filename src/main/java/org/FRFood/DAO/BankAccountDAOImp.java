package org.FRFood.DAO;

import org.FRFood.entity.BankAccount;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.DatabaseConnector;

import java.sql.*;
import java.util.Optional;

public class BankAccountDAOImp implements BankAccountDAO {
    @Override
    public int insertBankAccount(BankAccount bankAccount) throws SQLException {
        String temp = "INSERT INTO Bank_account (bank_name , account_number) VALUES (?,?)";
        int generatedId = -1;
        try (Connection connection = DatabaseConnector.gConnection();
                PreparedStatement statement = connection.prepareStatement(temp, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, bankAccount.getName());
            statement.setString(2, bankAccount.getAccountNumber());
            int updatedRows = statement.executeUpdate();
            if(updatedRows > 0){
                try (ResultSet generatedKeys = statement.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        generatedId = generatedKeys.getInt(1);
                    }else {
                        throw new SQLException("Creating category failed, no rows affected.");
                    }
                }
            } else{
                throw new SQLException("Creating category failed, no rows affected.");
            }
        }catch (SQLException e){
            if(e.getSQLState().equals("23000")){
                throw new DataAlreadyExistsException("Bank account already exists");
            }else{
                throw new DataAlreadyExistsException(e.getMessage());
            }
        }
        return 0;
    }

    @Override
    public Optional<BankAccount> getBankAccountByAccountNumber(String accountNumber)throws SQLException {
        BankAccount bankAccount = null;
        String temp = "SELECT * FROM bank_account WHERE account_number = ?";
        try (Connection connection = DatabaseConnector.gConnection();
            PreparedStatement statement = connection.prepareStatement(temp)){
            statement.setString(1, accountNumber);
            try (ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    bankAccount = new BankAccount();
                    bankAccount.setName(resultSet.getString("bank_name"));
                    bankAccount.setAccountNumber(resultSet.getString("account_number"));
                    bankAccount.setId(resultSet.getInt("id"));
                }
            }

        }
        return Optional.ofNullable(bankAccount);
    }

    @Override
    public Optional<BankAccount> getBankAccountById(int id) throws SQLException {
        BankAccount bankAccount = null;
        String temp = "SELECT * FROM bank_account WHERE id = ?";
        try(Connection connection = DatabaseConnector.gConnection();
            PreparedStatement statement = connection.prepareStatement(temp)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    bankAccount = new BankAccount();
                    bankAccount.setName(resultSet.getString("bank_name"));
                    bankAccount.setAccountNumber(resultSet.getString("account_number"));
                    bankAccount.setId(resultSet.getInt("id"));
                }
            }
        }
        return Optional.ofNullable(bankAccount);
    }
}
