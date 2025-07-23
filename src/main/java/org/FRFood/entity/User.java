package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.FRFood.util.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private Integer id;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("phone")
    private String phoneNumber;
    private String email;
    private String password;
    private String address;
    @JsonProperty("profileImageBase64")
    private String picture;
    private Role role;
    @JsonProperty("bank_info")
    private BankAccount bank;
    private boolean confirmed;
    private Integer wallet;

    public User(){};

    public User(String picture, String name, String email, String password, String address, String phoneNumber, Role role, BankAccount bank) {
        this.setAddress(address);
        this.setPassword(password);
        this.setEmail(email);
        this.setPicture(picture);
        this.setFullName(name);
        this.setPhoneNumber(phoneNumber);
        this.setRole(role);
        this.setBank(bank);
    }

    public String getAddress() {
        return address;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPicture() {
        return picture;
    }

    public BankAccount getBank() {
        return bank;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bank = bankAccount;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setBank(BankAccount bank) {
        this.bank = bank;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Integer getWallet() {
        return wallet;
    }

    public void setWallet(Integer wallet) {
        this.wallet = wallet;
    }
}