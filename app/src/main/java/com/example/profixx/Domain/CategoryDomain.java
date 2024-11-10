package com.example.profixx.Domain;

public class CategoryDomain {
    private String title;
    private String id;
    private String picUrl;
    private String businessName;
    private String logo;

    public CategoryDomain(String title, String id, String picUrl, String businessName) {
        this.title = title;
        this.id = id;
        this.picUrl = picUrl;
        this.businessName = businessName;
    }

    public CategoryDomain(){
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
