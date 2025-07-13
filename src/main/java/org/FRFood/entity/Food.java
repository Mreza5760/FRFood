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
    private int vendorId;
    private int price;
    private int supply;
    private List<Keyword> keywords;


    Food(String name,int vendorId , String description, List<Keyword> keywords, int priceId , int pictureId, Restaurant restaurant){
        this.vendorId = vendorId;
        this.name = name;
        this.description = description;
        this.price = priceId;
        this.keywords = keywords;
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
        return price;
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
}