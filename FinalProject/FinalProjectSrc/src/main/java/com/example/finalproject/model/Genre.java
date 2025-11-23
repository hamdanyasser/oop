package com.example.finalproject.model;

public class Genre {
    private int id;
    private String name;
    private String description;
    private String iconPath;

    public Genre() {}

    public Genre(int id, String name, String description, String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
    }

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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
        Genre genre = (Genre) obj;
        return id == genre.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
