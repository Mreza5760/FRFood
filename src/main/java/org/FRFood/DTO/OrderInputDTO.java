package org.FRFood.DTO;

import org.FRFood.entity.Order;

public class OrderInputDTO
{
    String deliveryAddress;
    int vendorId;
    int couponId;
    int restaurantId;
    ItemDTO items;

    OrderInputDTO(String deliveryAddress, int vendorId, int couponId, int restaurantId, ItemDTO items){
        this.deliveryAddress = deliveryAddress;
        this.vendorId = vendorId;
        this.couponId = couponId;
        this.restaurantId = restaurantId;
        this.items = items;
    }
    OrderInputDTO(){}

    public String getDeliveryAddress()
    {
        return deliveryAddress;
    }

    public int getCouponId() {
        return couponId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public int getVendorId() {
        return vendorId;
    }

    public ItemDTO getItems() {
        return items;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setItems(ItemDTO items) {
        this.items = items;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }
}
