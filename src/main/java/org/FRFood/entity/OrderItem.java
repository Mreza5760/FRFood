package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItem {
    @JsonProperty("item_id")
    private Integer itemId;
    private Integer quantity;

    public OrderItem(Integer itemId, Integer quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public OrderItem() {}

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}