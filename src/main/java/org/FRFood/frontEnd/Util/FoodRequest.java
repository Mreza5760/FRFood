package org.FRFood.frontEnd.Util;

import java.util.List;

public class FoodRequest {
    public String name;
    public String imageBase64;
    public String description;
    public double price;
    public int supply;
    public List<String> keywords;

    public FoodRequest(String name, String imageBase64, String description, double price, int supply, List<String> keywords) {
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.price = price;
        this.supply = supply;
        this.keywords = keywords;
    }
}