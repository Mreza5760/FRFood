package org.FRFood.frontEnd.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Rate {
    private Integer id;
    @JsonProperty("user_id")
    private Integer userId;
    private Integer rating;
    @JsonProperty("order_id")
    private Integer orderId;
    private String comment;
    @JsonProperty("created_id")
    private String createdAt;
    @JsonProperty("imageBase64")
    private List<String> images;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}