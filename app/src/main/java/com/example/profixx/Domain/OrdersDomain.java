package com.example.profixx.Domain;

import java.util.ArrayList;

public class OrdersDomain {
    String orderId;
    String businessId;
    String userName;
    String email;
    String itemName;
    String photoUrl;
    String phone;
    String address;
    String suburb;
    String city;
    String country;
    String postalCode;
    String province;
    String status;
    private int numItems;
    private int quantity;
    String userId;
    double totalAmount;
    String title;
    ArrayList<String> picUrl;
    double price;
    int NumberInCart;
    String itemId;


    public OrdersDomain() {
    }

    public OrdersDomain(String orderId, String businessId, String userName, String email, String itemName, String photoUrl, String phone, String address, String suburb, String city, String country, String postalCode, String province, String status, int numItems, int quantity, String userId, double totalAmount, String title, ArrayList<String> picUrl, double price, int numberInCart, String itemId) {
        this.orderId = orderId;
        this.businessId = businessId;
        this.userName = userName;
        this.email = email;
        this.itemName = itemName;
        this.photoUrl = photoUrl;
        this.phone = phone;
        this.address = address;
        this.suburb = suburb;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.province = province;
        this.status = status;
        this.numItems = numItems;
        this.quantity = quantity;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.title = title;
        this.picUrl = picUrl;
        this.price = price;
        this.NumberInCart = numberInCart;
        this.itemId = itemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumItems() {
        return numItems;
    }

    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getNumberInCart() {
        return NumberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        NumberInCart = numberInCart;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
