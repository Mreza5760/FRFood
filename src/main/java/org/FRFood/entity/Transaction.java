package org.FRFood.entity;

import org.FRFood.util.TransactionMethod;
import org.FRFood.util.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private int id;
    @JsonProperty("order_id")
    private int orderID;
    @JsonProperty("user_id")
    private int userID;
    private TransactionMethod method;
    private TransactionStatus status;
    private int amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public TransactionMethod getMethod() {
        return method;
    }

    public void setMethod(TransactionMethod method) {
        this.method = method;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
