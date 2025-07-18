package org.FRFood.entity;

public class Keyword {
    private Integer id;
    private String name;

    public Keyword(){};

    public Keyword(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
