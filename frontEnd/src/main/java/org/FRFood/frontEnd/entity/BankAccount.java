package org.FRFood.frontEnd.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

public class BankAccount implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    @JsonProperty("bank_name")
    private String name;
    @JsonProperty("account_number")
    private String accountNumber;

    public BankAccount(String name, String accountNumber) {
        this.name = name;
        this.accountNumber = accountNumber;
    }

    public BankAccount() {}

    public Integer getId() {
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
