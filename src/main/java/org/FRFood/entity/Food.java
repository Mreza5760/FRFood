package org.FRFood.entity;

import java.util.List;

public class Food {
    private int id;
    private String name;
    private String description;
    private List<Category> category;
    private int priceId;
    private String picture;
    private Restaurant restaurant;


    Food(String name , String description, List<Category> category , int priceId , int pictureId, Restaurant restaurant){
        this.name = name;
        this.description = description;
        this.priceId = priceId;
        this.category = category;
        this.restaurant = restaurant;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
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
        return priceId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<Category> getCategory() {
        return category;
    }

}
