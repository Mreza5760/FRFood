package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Menu {
    @JsonIgnore
    private Integer id;
    @JsonIgnore
    private Restaurant restaurant;
    private String title;

    Menu(Integer id, User owner,Restaurant restaurant, String title) {
        this.id = id;
        this.restaurant = restaurant;
        this.title = title;
    }

    public Menu() {}

    public Integer getId() {
        return id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public String getTitle() {
        return title;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}