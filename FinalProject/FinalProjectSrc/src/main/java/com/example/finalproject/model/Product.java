package com.example.finalproject.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private String description;
    private String imagePath;
    private int stock;

    private double discount;

    // Platform compatibility
    private List<String> platforms;
    private List<Integer> platformIds;

    // Genre tags
    private List<String> genres;
    private List<Integer> genreIds;

    public Product() {
        this.platforms = new ArrayList<>();
        this.platformIds = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.genreIds = new ArrayList<>();
    }

    public Product(int id, String name, String category, double price, String description, String imagePath, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imagePath = imagePath;
        this.stock = stock;
        this.platforms = new ArrayList<>();
        this.platformIds = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.genreIds = new ArrayList<>();
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

    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }

    public List<Integer> getPlatformIds() { return platformIds; }
    public void setPlatformIds(List<Integer> platformIds) { this.platformIds = platformIds; }

    public String getPlatformsAsString() {
        if (platforms == null || platforms.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", platforms);
    }

    public void addPlatform(int platformId, String platformName) {
        if (this.platformIds == null) this.platformIds = new ArrayList<>();
        if (this.platforms == null) this.platforms = new ArrayList<>();
        if (!this.platformIds.contains(platformId)) {
            this.platformIds.add(platformId);
            this.platforms.add(platformName);
        }
    }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }

    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", genres);
    }

    public void addGenre(int genreId, String genreName) {
        if (this.genreIds == null) this.genreIds = new ArrayList<>();
        if (this.genres == null) this.genres = new ArrayList<>();
        if (!this.genreIds.contains(genreId)) {
            this.genreIds.add(genreId);
            this.genres.add(genreName);
        }
    }

    public double getEffectivePrice() {
        return price * (1 - (discount / 100.0));
    }
}
