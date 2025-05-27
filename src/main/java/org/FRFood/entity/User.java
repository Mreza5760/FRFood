package org.FRFood.entity;

import org.FRFood.util.Role;

public class User {
    private int id;
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
    private String address;
    private String picture;
    private Role role;

    User(String picture, String name, String email, String password, String address, String phoneNumber, Role role){
        this.address = address;
        this.password = password;
        this.email = email;
        this.picture = picture;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
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
