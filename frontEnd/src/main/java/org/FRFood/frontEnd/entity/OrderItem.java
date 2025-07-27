package org.FRFood.frontEnd.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

public class OrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
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