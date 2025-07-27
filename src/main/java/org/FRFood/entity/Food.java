package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Food {
    private Integer id;
    private String name;
    @JsonProperty("imageBase64")
    private String picture;
    private String description;
    @JsonProperty("vendor_id")
    private Integer restaurantId;
    private Integer price;
    private Integer supply;
    private List<Keyword> keywords;


    public Food(String name, Integer vendorId, String description, List<Keyword> keywords, Integer price,String picture){
        this.setRestaurantId(vendorId);
        this.setName(name);
        this.setDescription(description);
        this.setPrice(price);
        this.setKeywords(keywords);
        this.setPicture(picture);
    }

    public Food(){}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getId() {
        return id;
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer vendorId) {
        this.restaurantId = vendorId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSupply() {
        return supply;
    }

    public void setSupply(Integer supply) {
        this.supply = supply;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(id, food.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}