package org.FRFood.entity;

import org.FRFood.util.Status;
import org.FRFood.DTO.OrderItemDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Order {
    private int id;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("customer_id")
    private int customerId;
    @JsonProperty("vendor_id")
    private int vendorId;
    @JsonProperty("coupon_id")
    private int couponId;
    private List<OrderItemDTO> items;
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
    private Status status;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    public Order(){}

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

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(int rawPrice) {
        this.rawPrice = rawPrice;
    }

    public int getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }

    public int getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(int additionalFee) {
        this.additionalFee = additionalFee;
    }

    public int getCourierFee() {
        return courierFee;
    }

    public void setCourierFee(int courierFee) {
        this.courierFee = courierFee;
    }

    public int getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(int payPrice) {
        this.payPrice = payPrice;
    }

    public int getCourierId() {
        return courierId;
    }

    public void setCourierId(int courierId) {
        this.courierId = courierId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}