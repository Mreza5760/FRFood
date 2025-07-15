package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankAccount {
    private int id;
    @JsonProperty("bank_name")
    private String name;
    @JsonProperty("account_number")
    private String accountNumber;

    public BankAccount(String name, String accountNumber) {
        this.name = name;
        this.accountNumber = accountNumber;
    }

    public BankAccount() {}

    public int getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
