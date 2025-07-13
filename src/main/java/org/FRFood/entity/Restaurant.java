package org.FRFood.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Restaurant {
    private int id;
    @JsonIgnore
    private User owner;
    private String name;
    private String address;
    private String phone;
    @JsonProperty("logoBase64")
    private String logo;
    @JsonProperty("tax_fee")
    private int taxFee;
    @JsonProperty("additional_fee")
    private int additionalFee;

    public Restaurant(User owner, String name, String address, String phone, String logo, int taxFee, int additionalFee) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logo = logo;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
    }

    public Restaurant() {}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAdditionalFee() {
        return additionalFee;
    }

    public int getTaxFee() {
        return taxFee;
    }

    public User getOwner() {
        return owner;
    }

    public String getAddress() {
        return address;
    }

    public String getLogo() {
        return logo;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAdditionalFee(int additionalFee) {
        this.additionalFee = additionalFee;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }
}

