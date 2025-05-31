package org.FRFood;

import org.FRFood.DAO.BankAccountDAO;
import org.FRFood.DAO.BankAccountDAOImp;
import org.FRFood.DAO.CategoryDAO;
import org.FRFood.DAO.CategoryDAOImp;
import org.FRFood.entity.BankAccount;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.DatabaseConnector;
import org.FRFood.entity.Category;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        CategoryDAO categoryDAO = new CategoryDAOImp();
        BankAccountDAO bankAccountDAO = new BankAccountDAOImp();
        System.out.println("Hello world!");
        try (Connection connection = DatabaseConnector.gConnection()) {
            System.out.println("connected to db");
            try (Statement statement = connection.createStatement()) {


                // test
//                try{
//                    BankAccount bankAccount = new BankAccount("account1","123456");
//                    bankAccountDAO.insertBankAccount(bankAccount);
//                    BankAccount bankAccount1 = new BankAccount("account2","123456542");
//                    bankAccountDAO.insertBankAccount(bankAccount1);
//                }catch(DataAlreadyExistsException e) {
//                    System.out.println(e.getMessage());
//                }
//                if(bankAccountDAO.getBankAccountByAccountNumber("123456").isPresent()){
//                    System.out.println(bankAccountDAO.getBankAccountByAccountNumber("123456").get().getName());
//                }
//                System.out.println(bankAccountDAO.getBankAccountById(1).get().getName());

                // end of test


            }

        } catch (SQLException e) {
            System.out.println("couldn't connect to db" + "\n" + e);
        }
    }
}

