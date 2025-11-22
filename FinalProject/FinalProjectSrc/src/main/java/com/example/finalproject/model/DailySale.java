package com.example.finalproject.model;

public class DailySale {
    private String date;
    private double revenue;

    public DailySale(String date, double revenue) {
        this.date = date;
        this.revenue = revenue;
    }

    public String getDate() { return date; }
    public double getRevenue() { return revenue; }
}
