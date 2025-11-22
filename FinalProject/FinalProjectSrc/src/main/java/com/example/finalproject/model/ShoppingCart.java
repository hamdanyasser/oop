package com.example.finalproject.model;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private static final Map<Integer, Integer> items = new HashMap<>();
    private static final Map<Integer, Double> prices = new HashMap<>();

    public static void addItem(int productId, double price) {
        items.put(productId, items.getOrDefault(productId, 0) + 1);
        prices.put(productId, price);
    }

    public static void addItem(int productId, double price, int quantity, int stock) {
        int current = items.getOrDefault(productId, 0);
        int newQty = Math.min(current + quantity, stock);
        items.put(productId, newQty);
        prices.put(productId, price);
    }

    public static Map<Integer, Integer> getItems() {
        return items;
    }

    public static double getProductPrice(int productId) {
        return prices.getOrDefault(productId, 0.0);
    }

    public static double getTotal() {
        return items.entrySet().stream()
                .mapToDouble(e -> prices.get(e.getKey()) * e.getValue())
                .sum();
    }

    public static void clear() {
        items.clear();
        prices.clear();
    }
}
