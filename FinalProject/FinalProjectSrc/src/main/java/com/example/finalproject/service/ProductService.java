package com.example.finalproject.service;

import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.model.Product;
import java.util.List;

public class ProductService {

    private final ProductDao dao = new ProductDao();

    public List<Product> getAll() {
        return dao.getAllProducts();
    }

    public void add(Product p) {
        validate(p);
        dao.insert(p);
    }

    public void update(Product p) {
        if (p.getId() <= 0)
            throw new IllegalArgumentException("Invalid product ID.");
        validate(p);
        dao.update(p);
    }

    public void delete(int id) {
        if (id <= 0)
            throw new IllegalArgumentException("Invalid ID.");
        dao.delete(id);
    }

    private void validate(Product p) {
        if (p.getName() == null || p.getName().trim().isEmpty())
            throw new IllegalArgumentException("Product name required.");
        if (p.getPrice() < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        if (p.getStock() < 0)
            throw new IllegalArgumentException("Stock cannot be negative.");
    }
}
