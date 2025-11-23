package com.example.finalproject.dao;

import com.example.finalproject.model.DigitalCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;




public class DigitalCodeDao {

    


    public void insert(DigitalCode digitalCode) {
        String sql = """
            INSERT INTO digital_codes (order_id, order_item_id, product_id, user_id, code, code_type, is_redeemed, sent_at, original_value, balance)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, digitalCode.getOrderId());
            ps.setInt(2, digitalCode.getOrderItemId());
            ps.setInt(3, digitalCode.getProductId());
            ps.setInt(4, digitalCode.getUserId());
            ps.setString(5, digitalCode.getCode());
            ps.setString(6, digitalCode.getCodeType());
            ps.setBoolean(7, digitalCode.isRedeemed());
            ps.setTimestamp(8, digitalCode.getSentAt());
            ps.setDouble(9, digitalCode.getOriginalValue());
            ps.setDouble(10, digitalCode.getBalance());

            ps.executeUpdate();

            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                digitalCode.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error inserting digital code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    


    public List<DigitalCode> getCodesByUserId(int userId) {
        List<DigitalCode> codes = new ArrayList<>();
        String sql = """
            SELECT * FROM digital_codes
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                codes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching codes for user: " + e.getMessage());
            e.printStackTrace();
        }

        return codes;
    }

    


    public List<DigitalCode> getCodesByOrderId(int orderId) {
        List<DigitalCode> codes = new ArrayList<>();
        String sql = """
            SELECT * FROM digital_codes
            WHERE order_id = ?
            ORDER BY created_at ASC
            """;

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                codes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching codes for order: " + e.getMessage());
            e.printStackTrace();
        }

        return codes;
    }

    


    public boolean isCodeValid(String code) {
        String sql = "SELECT is_redeemed FROM digital_codes WHERE code = ?";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return !rs.getBoolean("is_redeemed");
            }
        } catch (SQLException e) {
            System.err.println("Error checking code validity: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    


    public void redeemCode(String code) {
        String sql = "UPDATE digital_codes SET is_redeemed = TRUE, redeemed_at = CURRENT_TIMESTAMP WHERE code = ?";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error redeeming code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    


    public Optional<DigitalCode> getByCode(String code) {
        String sql = "SELECT * FROM digital_codes WHERE code = ?";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching code: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    


    public void updateGiftCardBalance(int codeId, double newBalance) {
        String sql = "UPDATE digital_codes SET balance = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, codeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating gift card balance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    


    private DigitalCode mapResultSet(ResultSet rs) throws SQLException {
        DigitalCode code = new DigitalCode(
            rs.getInt("id"),
            rs.getInt("order_id"),
            rs.getInt("order_item_id"),
            rs.getInt("product_id"),
            rs.getInt("user_id"),
            rs.getString("code"),
            rs.getString("code_type"),
            rs.getBoolean("is_redeemed"),
            rs.getTimestamp("redeemed_at"),
            rs.getTimestamp("sent_at"),
            rs.getTimestamp("created_at")
        );

        
        code.setOriginalValue(rs.getDouble("original_value"));
        code.setBalance(rs.getDouble("balance"));

        return code;
    }
}
