package com.example.finalproject.model;

public class Platform {
    private int id;
    private String name;
    private String type; // CONSOLE, PC, HANDHELD, MOBILE
    private String manufacturer;
    private String iconPath;

    public Platform() {}

    public Platform(int id, String name, String type, String manufacturer, String iconPath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.manufacturer = manufacturer;
        this.iconPath = iconPath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Platform platform = (Platform) obj;
        return id == platform.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
