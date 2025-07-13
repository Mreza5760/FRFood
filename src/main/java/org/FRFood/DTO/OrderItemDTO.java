package org.FRFood.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItemDTO {
    @JsonProperty("item_id")
    private int itemId;
    private int quantity;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
