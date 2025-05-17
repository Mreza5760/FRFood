package org.FRFood.entity;

public class Food {
    private int id;
    private String name;
    private String description;
    private String category;
    private int priceId;
    private int pictureId;

    Food(String name , String description, String category , int priceId , int pictureId){
        this.name = name;
        this.description = description;
        this.priceId = priceId;
        this.pictureId = pictureId;
        this.category = category;
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

    public int getPictureId() {
        return pictureId;
    }

    public int getPriceId() {
        return priceId;
    }
}
