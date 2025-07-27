package org.FRFood.frontEnd.entity;

import java.util.List;

public class ItemsReq {
    public String search;
    public int minPrice;
    public int maxPrice;
    public int minRate;
    public int maxRate;
    public List<String> keywords;
}