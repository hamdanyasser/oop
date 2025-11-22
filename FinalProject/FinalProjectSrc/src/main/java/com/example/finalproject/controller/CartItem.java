package com.example.finalproject.controller;

import com.example.finalproject.model.Product;
import javafx.beans.property.*;

public class CartItem {
    private final Product product;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty quantity;
    private final DoubleProperty total;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.name = new SimpleStringProperty(product.getName());
        this.price = new SimpleDoubleProperty(product.getPrice());
        this.quantity = new SimpleIntegerProperty(quantity);
        this.total = new SimpleDoubleProperty(product.getPrice() * quantity);
    }

    public Product getProduct() { return product; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty totalProperty() { return total; }
}
