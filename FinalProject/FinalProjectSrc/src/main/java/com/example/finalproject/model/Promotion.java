package com.example.finalproject.model;

import java.sql.Date;

public class Promotion {
    private int id;
    private Integer productId;
    private String category;
    private double discount;
    private Date startDate;
    private Date endDate;

    public Promotion() {}

    public Promotion(int id, Integer productId, String category, double discount, Date startDate, Date endDate) {
        this.id = id;
        this.productId = productId;
        this.category = category;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
