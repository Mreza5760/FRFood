package org.FRFood.entity;

public class Price {
    private int id;
    private int currentPrice;
    private int discount;

    public int getId() {
        return id;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public int getDiscount() {
        return discount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }
}