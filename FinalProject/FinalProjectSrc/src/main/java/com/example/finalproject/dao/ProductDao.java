package com.example.finalproject.dao;

import com.example.finalproject.model.Product;
import java.sql.*;
import java.util.*;

public class ProductDao {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();

        String sql = """
        SELECT p.*, 
               COALESCE(pr.discount, 0) AS discount
        FROM products p
        LEFT JOIN promotions pr
        ON (pr.product_id = p.id OR pr.category = p.category)
        AND CURDATE() BETWEEN pr.start_date AND pr.end_date
        ORDER BY p.id DESC
        """;

        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getInt("stock")
                );
                p.setDiscount(rs.getDouble("discount"));
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public Optional<Product> getById(int id) {
        String sql = "SELECT * FROM products WHERE id=?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void insert(Product p) {
        String sql = "INSERT INTO products(name,category,price,description,imagePath,stock) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getDescription());
            ps.setString(5, p.getImagePath());
            ps.setInt(6, p.getStock());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Product p) {
        String sql = """
                UPDATE products
                SET name=?, category=?, price=?, description=?, imagePath=?, stock=?
                WHERE id=?
                """;
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getDescription());
            ps.setString(5, p.getImagePath());
            ps.setInt(6, p.getStock());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decreaseStock(int productId, int quantity) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ?")) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getString("description"),
                rs.getString("imagePath"),
                rs.getInt("stock")
        );
        p.setDiscount(rs.getDouble("discount"));
        return p;
    }
}
