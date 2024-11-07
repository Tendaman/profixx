package com.example.profixx.Domain;

import java.io.Serializable;

public class CreateProductDomain implements Serializable {
    private String productName;
    private String oldPrice;
    private String newPrice;
    private String description;
    private String imageUrl;

    public CreateProductDomain() {
    }

    public CreateProductDomain(String productName, String oldPrice, String newPrice, String description, String imageUrl) {
        this.productName = productName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPrice = newPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
