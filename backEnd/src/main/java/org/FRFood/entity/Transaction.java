package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private Integer id;
    @JsonProperty("order_id")
    private Integer orderID;
    @JsonProperty("user_id")
    private Integer userID;
    private TransactionMethod method;
    private Integer amount;
    @JsonProperty("payed_at")
    private String payedAt;

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getPayedAt() {
        return payedAt;
    }

    public void setPayedAt(String payedAt) {
        this.payedAt = payedAt;
    }
}