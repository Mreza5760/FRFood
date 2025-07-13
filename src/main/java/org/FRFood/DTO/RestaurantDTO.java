package org.FRFood.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.FRFood.entity.User;

public class RestaurantDTO {
    private int id;
    private String name;
    private String address;
    private String phone;
    @JsonProperty("logoBase64")
    private String logo;
    @JsonProperty("tax_fee")
    private int taxFee;
    @JsonProperty("additional_fee")
    private int additionalFee;

    public RestaurantDTO( String name, String address, String phone, String logo, int taxFee, int additionalFee) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logo = logo;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
    }

    public RestaurantDTO() {}

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

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }
}

