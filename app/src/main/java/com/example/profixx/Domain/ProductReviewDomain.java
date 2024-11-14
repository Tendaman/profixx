package com.example.profixx.Domain;

public class ProductReviewDomain {
    String reviewId;
    String email;
    String phone;
    String photoUrl;
    int rating;
    String reviewText;
    String username;

    public ProductReviewDomain() {
    }

    public ProductReviewDomain(String reviewId, String email, String phone, String photoUrl, int rating, String reviewText, String username) {
        this.reviewId = reviewId;
        this.email = email;
        this.phone = phone;
        this.photoUrl = photoUrl;
        this.rating = rating;
        this.reviewText = reviewText;
        this.username = username;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
