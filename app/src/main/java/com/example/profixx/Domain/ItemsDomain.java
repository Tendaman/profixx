package com.example.profixx.Domain;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemsDomain implements Serializable {
    private String wishlistId;
    private String title;
    private String description;
    private ArrayList<String> picUrl;
    private double price;
    private Double oldPrice;
    private int review;
    private double rating;
    private int NumberInCart;
    private String itemId;
    private String businessId;

    public ItemsDomain() {
    }

    public ItemsDomain(String wishlistId, String title, String description, ArrayList<String> picUrl, double price, Double oldPrice, int review, double rating, String itemId, String businessId) {
        this.wishlistId = wishlistId;
        this.title = title;
        this.description = description;
        this.picUrl = picUrl;
        this.price = price;
        this.oldPrice = oldPrice;
        this.review = review;
        this.rating = rating;
        this.itemId = itemId;
        this.businessId = businessId;
    }

    public String getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(String wishlistId) {
        this.wishlistId = wishlistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(ArrayList<String> picUrl) {
        this.picUrl = picUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public int getReview() {
        return review;
    }

    public void setReview(int review) {
        this.review = review;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getNumberInCart() {
        return NumberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.NumberInCart = numberInCart;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
}