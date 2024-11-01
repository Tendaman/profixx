package com.example.profixx.Domain;

import java.io.Serializable;
import java.util.ArrayList;

public class WishlistDomain implements Serializable {
    private ArrayList<String> picUrl;
    private String title;
    private double price;
    private double rating;
    private String itemId;

    public WishlistDomain() {
    }

    public WishlistDomain(ArrayList<String> picUrl, String title, double price, double rating, String itemId) {
        this.picUrl = picUrl;
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.itemId = itemId;
    }

    public ArrayList<String> getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(ArrayList<String> picUrl) {
        this.picUrl = picUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
