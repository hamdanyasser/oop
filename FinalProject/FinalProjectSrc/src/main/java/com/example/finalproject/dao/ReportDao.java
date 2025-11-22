package com.example.finalproject.dao;

import com.example.finalproject.model.TopProduct;
import com.example.finalproject.model.DailySale;
import com.example.finalproject.model.CategorySale;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    public double getTotalRevenue() {
        String sql = "SELECT SUM(total) FROM orders WHERE status='DELIVERED'";
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<DailySale> getDailySales() {
        List<DailySale> list = new ArrayList<>();
        String sql = """
            SELECT DATE(created_at) AS day, SUM(total) AS daily_sales
            FROM orders
            WHERE status='DELIVERED'
            GROUP BY DATE(created_at)
            ORDER BY day ASC
        """;
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DailySale(rs.getString("day"), rs.getDouble("daily_sales")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<TopProduct> getTopSellingProducts() {
        List<TopProduct> list = new ArrayList<>();
        String sql = """
            SELECT p.name, SUM(oi.quantity) AS qty, SUM(oi.price * oi.quantity) AS revenue
            FROM order_items oi
            JOIN products p ON p.id = oi.product_id
            JOIN orders o ON o.id = oi.order_id
            WHERE o.status='DELIVERED'
            GROUP BY p.name
            ORDER BY qty DESC
        """;
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TopProduct(
                        rs.getString("name"),
                        rs.getInt("qty"),
                        rs.getDouble("revenue")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Get sales breakdown by product category for pie chart
     */
    public List<CategorySale> getCategorySales() {
        List<CategorySale> list = new ArrayList<>();
        String sql = """
            SELECT p.category, COUNT(DISTINCT p.id) AS product_count, SUM(oi.price * oi.quantity) AS revenue
            FROM order_items oi
            JOIN products p ON p.id = oi.product_id
            JOIN orders o ON o.id = oi.order_id
            WHERE o.status='DELIVERED'
            GROUP BY p.category
            ORDER BY revenue DESC
        """;
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new CategorySale(
                        rs.getString("category"),
                        rs.getInt("product_count"),
                        rs.getDouble("revenue")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Get total number of orders
     */
    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status='DELIVERED'";
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /**
     * Get total number of customers
     */
    public int getTotalCustomers() {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM orders WHERE status='DELIVERED'";
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}
