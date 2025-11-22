package com.example.finalproject.dao;

import com.example.finalproject.model.Promotion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionDao {

    public void insert(Promotion p) throws SQLException {
        String sql = "INSERT INTO promotions(product_id, category, discount, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (p.getProductId() != null)
                ps.setInt(1, p.getProductId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getDiscount());
            ps.setDate(4, p.getStartDate());
            ps.setDate(5, p.getEndDate());
            ps.executeUpdate();
        }
    }

    public List<Promotion> getAll() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotions ORDER BY id DESC";
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void delete(int id) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM promotions WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Promotion map(ResultSet rs) throws SQLException {
        return new Promotion(
                rs.getInt("id"),
                (Integer) rs.getObject("product_id"),
                rs.getString("category"),
                rs.getDouble("discount"),
                rs.getDate("start_date"),
                rs.getDate("end_date")
        );
    }
}
