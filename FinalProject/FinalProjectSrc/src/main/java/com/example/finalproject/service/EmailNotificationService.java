package com.example.finalproject.service;

import com.example.finalproject.model.Order;
import com.example.finalproject.model.OrderItem;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.User;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.util.EmailSender;

import java.text.SimpleDateFormat;

/**
 * Centralized service for sending email notifications to users
 */
public class EmailNotificationService {

    private final ProductDao productDao = new ProductDao();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");

    /**
     * Send welcome email to newly registered users
     */
    public boolean sendWelcomeEmail(User user) {
        String subject = "Welcome to ShopEase - Your Gaming Store! 🎮";

        String body = String.format("""
                Hi %s,

                Welcome to ShopEase! 🎉

                Thank you for creating an account with us. We're excited to have you as part of our gaming community!

                Here's what you can do with your account:
                • Browse thousands of games and accessories
                • Purchase physical and digital products
                • Buy gift cards for friends and family
                • Track your orders and download digital codes
                • Add items to your wishlist
                • Leave reviews on products

                Start exploring our collection and find your next favorite game!

                Happy Gaming! 🎮

                Best regards,
                The ShopEase Team

                ---
                This is an automated message. Please do not reply to this email.
                """, user.getUsername());

        return EmailSender.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send order confirmation email with full order details
     */
    public boolean sendOrderConfirmationEmail(User user, Order order) {
        String subject = "Order Confirmation #" + order.getId() + " - ShopEase";

        StringBuilder itemsList = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            Product product = productDao.getById(item.getProductId()).orElse(null);
            if (product != null) {
                String productType = product.isGiftCard() ? " [Gift Card]"
                                   : product.isDigital() ? " [Digital]"
                                   : "";
                itemsList.append(String.format("  • %s%s\n    Quantity: %d | Price: $%.2f | Subtotal: $%.2f\n\n",
                    product.getName(),
                    productType,
                    item.getQuantity(),
                    item.getPrice(),
                    item.getPrice() * item.getQuantity()
                ));
            }
        }

        String body = String.format("""
                Hi %s,

                Thank you for your order! 🎉

                ORDER DETAILS
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Order Number: #%d
                Order Date: %s
                Order Status: %s

                ITEMS ORDERED
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                %s
                TOTAL AMOUNT: $%.2f
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                What's Next?
                • Physical items will be shipped to your address
                • Digital codes have been sent in a separate email (if applicable)
                • You can track your order status in your account

                Questions? Contact our support team anytime.

                Thank you for shopping with ShopEase!

                Best regards,
                The ShopEase Team

                ---
                This is an automated message. Please do not reply to this email.
                """,
                user.getUsername(),
                order.getId(),
                dateFormat.format(order.getCreatedAt()),
                order.getStatus(),
                itemsList.toString(),
                order.getTotal()
        );

        return EmailSender.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send shipping notification when order is marked as delivered
     */
    public boolean sendShippingNotificationEmail(User user, Order order) {
        String subject = "Your Order #" + order.getId() + " Has Been Delivered! 📦";

        StringBuilder itemsList = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            Product product = productDao.getById(item.getProductId()).orElse(null);
            if (product != null) {
                itemsList.append(String.format("  • %s (×%d)\n",
                    product.getName(),
                    item.getQuantity()
                ));
            }
        }

        String body = String.format("""
                Hi %s,

                Great news! Your order has been delivered! 📦✨

                ORDER INFORMATION
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Order Number: #%d
                Delivery Date: %s

                DELIVERED ITEMS
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                %s
                We hope you enjoy your purchase!

                If you have any issues with your order, please contact our support team.

                Don't forget to leave a review! Your feedback helps other gamers make informed decisions.

                Thank you for choosing ShopEase!

                Best regards,
                The ShopEase Team

                ---
                This is an automated message. Please do not reply to this email.
                """,
                user.getUsername(),
                order.getId(),
                new SimpleDateFormat("MMM dd, yyyy").format(System.currentTimeMillis()),
                itemsList.toString()
        );

        return EmailSender.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send order status update email
     */
    public boolean sendOrderStatusUpdateEmail(User user, Order order, String previousStatus) {
        String subject = "Order #" + order.getId() + " Status Update";

        String statusMessage = switch (order.getStatus().toUpperCase()) {
            case "PENDING" -> "Your order has been received and is being processed.";
            case "PROCESSING" -> "Your order is being prepared for shipment.";
            case "SHIPPED" -> "Your order has been shipped and is on its way!";
            case "DELIVERED" -> "Your order has been delivered!";
            case "CANCELLED" -> "Your order has been cancelled.";
            default -> "Your order status has been updated.";
        };

        String statusIcon = switch (order.getStatus().toUpperCase()) {
            case "PENDING" -> "⏳";
            case "PROCESSING" -> "⚙️";
            case "SHIPPED" -> "🚚";
            case "DELIVERED" -> "✅";
            case "CANCELLED" -> "❌";
            default -> "📦";
        };

        String body = String.format("""
                Hi %s,

                Your order status has been updated! %s

                ORDER STATUS UPDATE
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Order Number: #%d
                Previous Status: %s
                New Status: %s

                %s

                You can view your complete order details by logging into your account.

                Thank you for shopping with ShopEase!

                Best regards,
                The ShopEase Team

                ---
                This is an automated message. Please do not reply to this email.
                """,
                user.getUsername(),
                statusIcon,
                order.getId(),
                previousStatus,
                order.getStatus(),
                statusMessage
        );

        return EmailSender.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send promotional email (requires user opt-in)
     */
    public boolean sendPromotionalEmail(User user, String promoTitle, String promoMessage) {
        String subject = "Special Offer from ShopEase - " + promoTitle;

        String body = String.format("""
                Hi %s,

                %s

                Visit ShopEase to take advantage of this offer!

                Happy Gaming! 🎮

                Best regards,
                The ShopEase Team

                ---
                You're receiving this because you opted in to promotional emails.
                To unsubscribe, please update your preferences in your account settings.
                """,
                user.getUsername(),
                promoMessage
        );

        return EmailSender.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset Request - ShopEase";

        String body = String.format("""
                Hi %s,

                We received a request to reset your password for your ShopEase account.

                Your password reset code is: %s

                If you didn't request this password reset, please ignore this email.
                Your password will remain unchanged.

                For security reasons, this code will expire in 24 hours.

                Best regards,
                The ShopEase Team

                ---
                This is an automated message. Please do not reply to this email.
                """,
                user.getUsername(),
                resetToken
        );

        return EmailSender.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send low stock alert to admin
     */
    public boolean sendLowStockAlertEmail(String adminEmail, Product product) {
        String subject = "⚠️ Low Stock Alert - " + product.getName();

        String body = String.format("""
                ALERT: Low Stock Warning

                The following product is running low on stock:

                Product: %s
                Category: %s
                Current Stock: %d
                Product ID: %d

                Please restock this item to avoid running out.

                ---
                ShopEase Admin System
                """,
                product.getName(),
                product.getCategory(),
                product.getStock(),
                product.getId()
        );

        return EmailSender.sendEmail(adminEmail, subject, body);
    }
}
