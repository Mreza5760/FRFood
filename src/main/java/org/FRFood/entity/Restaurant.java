package org.FRFood.entity;

public class Restaurant {
    private int id;
    private Person owner;
    private String name;
    private String address;
    private String phone;
    private String logo;
    private int taxFee;
    private int additionalFee;

    Restaurant(Person owner, String name,String address,String phone,String logo,int taxFee,int additionalFee) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logo = logo;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
    }

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

    public Person getOwner() {
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

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }
}

