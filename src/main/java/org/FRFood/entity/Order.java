package org.FRFood.entity;

import java.util.Map;

public class Order {
    private int id;
    private int vendorId;
    private int couponId;
    private String deliveryAddress;
    private Map<Food, Integer> foodList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Map<Food, Integer> getFoodList() {
        return foodList;
    }

    public void setFoodList(Map<Food, Integer> foodList) {
        this.foodList = foodList;
    }
}