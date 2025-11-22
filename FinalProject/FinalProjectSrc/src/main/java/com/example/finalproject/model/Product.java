package com.example.finalproject.model;

public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private String description;
    private String imagePath;
    private int stock;

    private double discount;

    public Product() {}

    public Product(int id, String name, String category, double price, String description, String imagePath, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imagePath = imagePath;
        this.stock = stock;
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public double getEffectivePrice() {
        return price * (1 - (discount / 100.0));
    }
}
