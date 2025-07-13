package org.FRFood.entity;

import java.util.Map;

public class Order {
    private int id;
    private int vendorId;
    private int couponId;
    private String deliveryAddress;
    private Map<Food, Integer> foodList;
}