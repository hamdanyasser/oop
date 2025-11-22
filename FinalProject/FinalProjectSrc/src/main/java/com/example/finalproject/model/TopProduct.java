package com.example.finalproject.model;

public class TopProduct {
    private String productName;
    private int quantitySold;
    private double revenue;

    public TopProduct(String productName, int quantitySold, double revenue) {
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public String getProductName() { return productName; }
    public int getQuantitySold() { return quantitySold; }
    public double getRevenue() { return revenue; }
}
