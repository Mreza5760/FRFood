package org.FRFood.entity;

import org.FRFood.util.TransactionMethod;
import org.FRFood.util.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private Integer id;
    @JsonProperty("order_id")
    private Integer orderID;
    @JsonProperty("user_id")
    private Integer userID;
    private TransactionMethod method;
    private TransactionStatus status;
    private Integer amount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
