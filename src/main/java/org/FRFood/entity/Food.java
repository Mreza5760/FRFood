package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Food {
    private int id;
    private String name;
    @JsonProperty("imageBase64")
    private String picture;
    private String description;
    @JsonProperty("vendor_id")
    private int restaurantId;
    private int price;
    private int supply;
    private List<Keyword> keywords;

    public Food() {}

    public Food(String name, int restaurantId, String description, List<Keyword> keywords, int price, int pictureId, Restaurant restaurant){
        this.setRestaurantId(restaurantId);
        this.setName(name);
        this.setDescription(description);
        this.setPrice(price);
        this.setKeywords(keywords);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public int getPriceId() {
        return getPrice();
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }
}