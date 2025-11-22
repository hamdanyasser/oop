package com.example.finalproject.dao;

import com.example.finalproject.model.Shipping;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShippingDao {

    public void createShipping(int orderId, String address) throws SQLException {
        String sql = "INSERT INTO shipping(order_id, address, status, shipped_at) VALUES(?,?, 'Pending', NOW())";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, address);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = switch (status) {
            case "Delivered" -> "UPDATE shipping SET status='Delivered', delivered_at=NOW() WHERE order_id=?";
            case "In Transit" -> "UPDATE shipping SET status='In Transit' WHERE order_id=?";
            default -> "UPDATE shipping SET status='Pending' WHERE order_id=?";
        };
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    public Shipping findByOrder(int orderId) {
        String sql = "SELECT * FROM shipping WHERE order_id=?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Shipping(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getTimestamp("shipped_at"),
                        rs.getTimestamp("delivered_at")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Shipping> findAll() {
        List<Shipping> list = new ArrayList<>();
        String sql = "SELECT * FROM shipping ORDER BY shipped_at DESC";
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Shipping(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getTimestamp("shipped_at"),
                        rs.getTimestamp("delivered_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
