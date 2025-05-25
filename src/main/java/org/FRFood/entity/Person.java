package org.FRFood.entity;

public class Person {
    private int id;
    private String name;
    private String phoneNumber;
    private String email = null;
    private String password;
    private String address;
    private String picture;

    Person(String picture,String name,String email,String password,String address,String phoneNumber){
        this.address = address;
        this.password = password;
        this.email = email;
        this.picture = picture;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPicture() {
        return picture;
    }
}
