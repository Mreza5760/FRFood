package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({
        "id",
        "delivery_address",
        "customer_id",
        "vendor_id",
        "coupon_id",
        "item_ids",
        "raw_price",
        "tax_fee",
        "additional_fee",
        "courier_fee",
        "pay_price",
        "courier_id",
        "status",
        "created_at",
        "updated_at"
})
public class Order {
    private Integer id;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("customer_id")
    private Integer customerId;
    @JsonProperty("vendor_id")
    private Integer restaurantId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    private List<OrderItem> items;
    @JsonProperty("raw_price")
    private Integer rawPrice;
    @JsonProperty("tax_fee")
    private Integer taxFee;
    @JsonProperty("additional_fee")
    private Integer additionalFee;
    @JsonProperty("courier_fee")
    private Integer courierFee;
    @JsonProperty("pay_price")
    private Integer payPrice;
    @JsonProperty("courier_id")
    private Integer courierId;
    private Status status;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    public Order(){
        items = new ArrayList<OrderItem>();
        rawPrice = 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public Integer getCouponId() { return couponId; }
    public void setCouponId(Integer couponId) { this.couponId = couponId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(Integer rawPrice) {
        this.rawPrice = rawPrice;
    }

    public Integer getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(Integer taxFee) {
        this.taxFee = taxFee;
    }

    public Integer getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Integer additionalFee) {
        this.additionalFee = additionalFee;
    }

    public Integer getCourierFee() {
        return courierFee;
    }

    public void setCourierFee(Integer courierFee) {
        this.courierFee = courierFee;
    }

    public Integer getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(Integer payPrice) {
        this.payPrice = payPrice;
    }

    public Integer getCourierId() {
        return courierId;
    }

    public void setCourierId(Integer courierId) {
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

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
