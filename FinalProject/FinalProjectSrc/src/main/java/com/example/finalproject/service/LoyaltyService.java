package com.example.finalproject.service;

import com.example.finalproject.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Loyalty Points System Service
 * Manages customer loyalty points and rewards
 *
 * Points System:
 * - Earn: 10 points per $1 spent
 * - Redeem: 1000 points = $10 discount
 * - Minimum redemption: 100 points ($1 discount)
 */
public class LoyaltyService {

    private static final int POINTS_PER_DOLLAR = 10;
    private static final int POINTS_FOR_TEN_DOLLARS = 1000;
    private static final int MIN_REDEMPTION_POINTS = 100;

    /**
     * Get user's current loyalty points balance
     */
    public int getPoints(int userId) {
        String query = "SELECT loyalty_points FROM users WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
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

    /**
     * Award points to user based on purchase amount
     *
     * @param userId User ID
     * @param orderTotal Order total amount
     * @param orderId Order ID for tracking
     * @return Points earned
     */
    public int awardPoints(int userId, double orderTotal, int orderId) {
        int pointsEarned = (int) (orderTotal * POINTS_PER_DOLLAR);

        if (pointsEarned <= 0) {
            return 0;
        }

        // Update user's total points
        String updateQuery = "UPDATE users SET loyalty_points = loyalty_points + ? WHERE id = ?";

        // Record transaction
        String transactionQuery = "INSERT INTO loyalty_transactions " +
                "(user_id, order_id, points, transaction_type, description) " +
                "VALUES (?, ?, ?, 'EARNED', ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Update points balance
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, pointsEarned);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            // Record transaction
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

    /**
     * Redeem points for discount
     *
     * @param userId User ID
     * @param pointsToRedeem Points to redeem
     * @param orderId Order ID for tracking
     * @return Discount amount in dollars
     */
    public double redeemPoints(int userId, int pointsToRedeem, int orderId) {
        // Validate redemption amount
        if (pointsToRedeem < MIN_REDEMPTION_POINTS) {
            return 0;
        }

        // Check if user has enough points
        int currentPoints = getPoints(userId);
        if (currentPoints < pointsToRedeem) {
            return 0;
        }

        // Calculate discount
        double discount = calculateDiscount(pointsToRedeem);

        // Deduct points
        String updateQuery = "UPDATE users SET loyalty_points = loyalty_points - ? WHERE id = ?";

        // Record transaction
        String transactionQuery = "INSERT INTO loyalty_transactions " +
                "(user_id, order_id, points, transaction_type, description) " +
                "VALUES (?, ?, ?, 'REDEEMED', ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Deduct points
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, pointsToRedeem);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            // Record transaction
            try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, orderId);
                stmt.setInt(3, -pointsToRedeem); // Negative for redemption
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

    /**
     * Calculate discount amount from points
     * 1000 points = $10
     * 100 points = $1
     */
    public double calculateDiscount(int points) {
        return (points / (double) POINTS_FOR_TEN_DOLLARS) * 10.0;
    }

    /**
     * Calculate points needed for a specific discount
     */
    public int calculatePointsNeeded(double discountAmount) {
        return (int) Math.ceil((discountAmount / 10.0) * POINTS_FOR_TEN_DOLLARS);
    }

    /**
     * Check if user has enough points for redemption
     */
    public boolean canRedeem(int userId, int points) {
        return points >= MIN_REDEMPTION_POINTS && getPoints(userId) >= points;
    }

    /**
     * Get maximum discount user can get with their current points
     */
    public double getMaxDiscount(int userId) {
        int points = getPoints(userId);
        if (points < MIN_REDEMPTION_POINTS) {
            return 0;
        }
        return calculateDiscount(points);
    }

    /**
     * Get points tier/level for gamification
     */
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

    /**
     * Format points with commas for display
     */
    public String formatPoints(int points) {
        return String.format("%,d", points);
    }

    /**
     * Get minimum redemption points
     */
    public int getMinRedemptionPoints() {
        return MIN_REDEMPTION_POINTS;
    }

    /**
     * Get points per dollar rate
     */
    public int getPointsPerDollar() {
        return POINTS_PER_DOLLAR;
    }
}
