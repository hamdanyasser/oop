package com.example.finalproject.model;

public class CategorySale {
    private String category;
    private int count;
    private double revenue;

    public CategorySale(String category, int count, double revenue) {
        this.category = category;
        this.count = count;
        this.revenue = revenue;
    }

    public String getCategory() { return category; }
    public int getCount() { return count; }
    public double getRevenue() { return revenue; }
}
