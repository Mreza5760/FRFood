package org.FRFood.entity;

import java.util.List;

public class ItemsReq {
    public String search;
    public int minPrice;
    public int maxPrice;
    public List<String> keywords;
}