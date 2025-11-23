package com.example.finalproject.service;

import com.example.finalproject.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;










public class LoyaltyService {

    private static final int POINTS_PER_DOLLAR = 10;
    private static final int POINTS_FOR_TEN_DOLLARS = 1000;
    private static final int MIN_REDEMPTION_POINTS = 100;

    


    public int getPoints(int userId) {
        String query = "SELECT loyalty_points FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("loyalty_points");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    







    public int awardPoints(int userId, double orderTotal, int orderId) {
        int pointsEarned = (int) (orderTotal * POINTS_PER_DOLLAR);

        if (pointsEarned <= 0) {
            return 0;
        }

        
        String updateQuery = "UPDATE users SET loyalty_points = loyalty_points + ? WHERE id = ?";

        
        String transactionQuery = "INSERT INTO loyalty_transactions " +
                "(user_id, order_id, points, transaction_type, description) " +
                "VALUES (?, ?, ?, 'EARNED', ?)";

        try (Connection conn = DBConnection.getInstance()) {
            
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, pointsEarned);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            
            try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, orderId);
                stmt.setInt(3, pointsEarned);
                stmt.setString(4, String.format("Earned %d points from order #%d ($%.2f)",
                        pointsEarned, orderId, orderTotal));
                stmt.executeUpdate();
            }

            return pointsEarned;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    







    public double redeemPoints(int userId, int pointsToRedeem, int orderId) {
        
        if (pointsToRedeem < MIN_REDEMPTION_POINTS) {
            return 0;
        }

        
        int currentPoints = getPoints(userId);
        if (currentPoints < pointsToRedeem) {
            return 0;
        }

        
        double discount = calculateDiscount(pointsToRedeem);

        
        String updateQuery = "UPDATE users SET loyalty_points = loyalty_points - ? WHERE id = ?";

        
        String transactionQuery = "INSERT INTO loyalty_transactions " +
                "(user_id, order_id, points, transaction_type, description) " +
                "VALUES (?, ?, ?, 'REDEEMED', ?)";

        try (Connection conn = DBConnection.getInstance()) {
            
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, pointsToRedeem);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            
            try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, orderId);
                stmt.setInt(3, -pointsToRedeem); 
                stmt.setString(4, String.format("Redeemed %d points for $%.2f discount on order #%d",
                        pointsToRedeem, discount, orderId));
                stmt.executeUpdate();
            }

            return discount;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    




    public double calculateDiscount(int points) {
        return (points / (double) POINTS_FOR_TEN_DOLLARS) * 10.0;
    }

    


    public int calculatePointsNeeded(double discountAmount) {
        return (int) Math.ceil((discountAmount / 10.0) * POINTS_FOR_TEN_DOLLARS);
    }

    


    public boolean canRedeem(int userId, int points) {
        return points >= MIN_REDEMPTION_POINTS && getPoints(userId) >= points;
    }

    


    public double getMaxDiscount(int userId) {
        int points = getPoints(userId);
        if (points < MIN_REDEMPTION_POINTS) {
            return 0;
        }
        return calculateDiscount(points);
    }

    


    public String getPointsTier(int points) {
        if (points >= 5000) {
            return "💎 Diamond";
        } else if (points >= 2500) {
            return "🥇 Gold";
        } else if (points >= 1000) {
            return "🥈 Silver";
        } else if (points >= 500) {
            return "🥉 Bronze";
        } else {
            return "🌟 Member";
        }
    }

    


    public String formatPoints(int points) {
        return String.format("%,d", points);
    }

    


    public int getMinRedemptionPoints() {
        return MIN_REDEMPTION_POINTS;
    }

    


    public int getPointsPerDollar() {
        return POINTS_PER_DOLLAR;
    }
}
