package com.example.finalproject.service;

import com.example.finalproject.model.Product;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartService {
    private static CartService instance;
    private final Map<Product, Integer> items = new LinkedHashMap<>();

    private CartService() {}

    public static CartService getInstance() {
        if (instance == null) instance = new CartService();
        return instance;
    }

    public Map<Product, Integer> getItems() {
        return items;
    }

    public void addItem(Product p) {
        items.put(p, items.getOrDefault(p, 0) + 1);
    }
    public void addItem(Product p, int quantity) {
        int currentQty = items.getOrDefault(p, 0);
        int newQty = Math.min(currentQty + quantity, p.getStock());
        items.put(p, newQty);
    }


    public void removeItem(Product p) {
        items.remove(p);
    }

    public void clear() {
        items.clear();
    }

    public double getTotal() {
        return items.entrySet().stream()
                .mapToDouble(e -> e.getKey().getEffectivePrice() * e.getValue())
                .sum();
    }
}
