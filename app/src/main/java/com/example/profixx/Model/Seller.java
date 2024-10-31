package com.example.profixx.Model;

// Seller model class
public class Seller {
    private String sellerId;
    private String businessName;
    private String businessEmail;
    private String phoneNumber;
    private String description;
    private String logoUrl;
    private long registrationDate;

    // Required empty constructor for Firebase
    public Seller() {}

    public Seller(String sellerId, String businessName, String businessEmail,
                  String phoneNumber, String description, String logoUrl, long registrationDate) {
        this.sellerId = sellerId;
        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.logoUrl = logoUrl;
        this.registrationDate = registrationDate;
    }

    // Getters and setters
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public long getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(long registrationDate) { this.registrationDate = registrationDate; }
}
