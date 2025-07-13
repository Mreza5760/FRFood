package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.FRFood.DTO.OrderItemDTO;

import java.util.List;
import java.util.Map;

public class Order {
    private int id;
    @JsonProperty("customer_id")
    private int customerId;
    @JsonProperty("vendor_id")
    private int vendorId;
    @JsonProperty("coupon_id")
    private int couponId;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("raw_price")
    private int rawPrice;
    @JsonProperty("tax_fee")
    private int taxFee;
    @JsonProperty("additional_fee")
    private int additionalFee;
    @JsonProperty("courier_fee")
    private int courierFee;
    @JsonProperty("pay_price")
    private int payPrice;
    @JsonProperty("courier_id")
    private int courierId;
    private String status;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    private List<OrderItemDTO> items;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

}