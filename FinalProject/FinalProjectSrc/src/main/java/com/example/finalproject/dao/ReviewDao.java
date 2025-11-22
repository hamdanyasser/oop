package com.example.finalproject.dao;

import com.example.finalproject.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    public void saveReview(Review review) throws SQLException {
        Connection conn = DBConnection.getInstance();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO review(product_id, user_id, rating, comment) VALUES(?,?,?,?)");
        ps.setInt(1, review.getProductId());
        ps.setInt(2, review.getUserId());
        ps.setInt(3, review.getRating());
        ps.setString(4, review.getComment());
        ps.executeUpdate();
    }

    public List<Review> getReviewsByProduct(int productId) {
        List<Review> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM review WHERE product_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getAverageRating(int productId) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT AVG(rating) AS avg FROM review WHERE product_id=?")) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("avg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<Review> getReviewsByProductWithUser(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name AS username FROM review r " +
                "JOIN users u ON r.user_id = u.id WHERE r.product_id=? ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Review r = new Review(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at")
                );
                r.setUsername(rs.getString("username"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Review> getAllReviewsForAdmin() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name AS username, p.name AS product_name " +
                "FROM review r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN products p ON r.product_id = p.id " +
                "ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Review r = new Review(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at")
                );
                r.setUsername(rs.getString("username"));
                r.setProductName(rs.getString("product_name"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


}
