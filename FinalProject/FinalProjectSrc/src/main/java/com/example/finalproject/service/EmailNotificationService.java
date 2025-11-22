package com.example.finalproject.service;

import com.example.finalproject.model.Order;
import com.example.finalproject.model.OrderItem;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.User;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.util.EmailSender;

import java.text.SimpleDateFormat;

/**
 * Centralized service for sending modern HTML email notifications to users
 */
public class EmailNotificationService {

    private final ProductDao productDao = new ProductDao();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");

    /**
     * Base HTML template wrapper for all emails
     */
    private String wrapInTemplate(String content) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f7fa;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        color: #ffffff;
                        font-size: 32px;
                        font-weight: 700;
                    }
                    .header .icon {
                        font-size: 48px;
                        margin-bottom: 10px;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .button {
                        display: inline-block;
                        padding: 14px 32px;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: #ffffff !important;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 16px;
                        margin: 20px 0;
                    }
                    .button:hover {
                        opacity: 0.9;
                    }
                    .info-box {
                        background-color: #f0f3ff;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .success-box {
                        background-color: #e8f5e9;
                        border-left: 4px solid #28a745;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning-box {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .order-table {
                        width: 100%%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }
                    .order-table th {
                        background-color: #f8f9fa;
                        padding: 12px;
                        text-align: left;
                        font-weight: 600;
                        color: #2c3e50;
                        border-bottom: 2px solid #dee2e6;
                    }
                    .order-table td {
                        padding: 12px;
                        border-bottom: 1px solid #dee2e6;
                    }
                    .total-row {
                        background-color: #f0f3ff;
                        font-weight: 700;
                        font-size: 18px;
                        color: #667eea;
                    }
                    .footer {
                        background-color: #2c3e50;
                        color: #ffffff;
                        padding: 30px;
                        text-align: center;
                        font-size: 14px;
                    }
                    .footer a {
                        color: #667eea;
                        text-decoration: none;
                    }
                    .badge {
                        display: inline-block;
                        padding: 4px 12px;
                        border-radius: 12px;
                        font-size: 12px;
                        font-weight: 600;
                        margin-left: 8px;
                    }
                    .badge-digital {
                        background-color: #e3f2fd;
                        color: #1976d2;
                    }
                    .badge-gift {
                        background-color: #fce4ec;
                        color: #c2185b;
                    }
                    .divider {
                        height: 1px;
                        background-color: #dee2e6;
                        margin: 30px 0;
                    }
                    h2 {
                        color: #2c3e50;
                        font-size: 24px;
                        margin-top: 0;
                    }
                    p {
                        color: #6c757d;
                        line-height: 1.6;
                        font-size: 15px;
                    }
                    ul {
                        color: #6c757d;
                        line-height: 1.8;
                    }
                    .code-box {
                        background-color: #f8f9fa;
                        border: 2px dashed #667eea;
                        padding: 20px;
                        text-align: center;
                        border-radius: 8px;
                        margin: 20px 0;
                    }
                    .code {
                        font-family: 'Courier New', monospace;
                        font-size: 20px;
                        font-weight: 700;
                        color: #667eea;
                        letter-spacing: 2px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    %s
                    <div class="footer">
                        <p style="margin: 0 0 10px 0; color: #ffffff;">
                            <strong>ShopEase Gaming Store</strong>
                        </p>
                        <p style="margin: 0 0 10px 0; color: #95a5a6;">
                            Your one-stop shop for all gaming needs
                        </p>
                        <p style="margin: 0; color: #95a5a6; font-size: 12px;">
                            This is an automated message, please do not reply to this email.<br>
                            © 2025 ShopEase. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, content);
    }

    /**
     * Send welcome email to newly registered users
     */
    public boolean sendWelcomeEmail(User user) {
        String subject = "Welcome to ShopEase - Your Gaming Store! 🎮";

        String content = String.format("""
            <div class="header">
                <div class="icon">🎮</div>
                <h1>Welcome to ShopEase!</h1>
            </div>
            <div class="content">
                <h2>Hi %s! 👋</h2>

                <p>Welcome to ShopEase! We're thrilled to have you join our gaming community.</p>

                <div class="success-box">
                    <strong>🎉 Your account is now active!</strong><br>
                    You can start exploring our collection of games, accessories, and more.
                </div>

                <h3 style="color: #2c3e50; margin-top: 30px;">What you can do with your account:</h3>
                <ul>
                    <li><strong>Browse</strong> thousands of games and accessories</li>
                    <li><strong>Purchase</strong> physical and digital products</li>
                    <li><strong>Buy</strong> gift cards for friends and family</li>
                    <li><strong>Track</strong> your orders and download digital codes</li>
                    <li><strong>Earn</strong> loyalty points with every purchase</li>
                    <li><strong>Create</strong> wishlists and leave reviews</li>
                </ul>

                <div style="text-align: center;">
                    <a href="#" class="button">Start Shopping Now</a>
                </div>

                <div class="divider"></div>

                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                    Need help? Contact our support team anytime at support@shopease.com
                </p>
            </div>
            """, user.getUsername());

        return EmailSender.sendHtmlEmail(user.getEmail(), subject, wrapInTemplate(content));
    }

    /**
     * Send order confirmation email with full order details
     */
    public boolean sendOrderConfirmationEmail(User user, Order order) {
        String subject = "Order Confirmation #" + order.getId() + " - ShopEase";

        StringBuilder itemsTable = new StringBuilder();
        itemsTable.append("""
            <table class="order-table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Qty</th>
                        <th>Price</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
            """);

        for (OrderItem item : order.getItems()) {
            Product product = productDao.getById(item.getProductId()).orElse(null);
            if (product != null) {
                String badge = "";
                if (product.isGiftCard()) {
                    badge = "<span class='badge badge-gift'>Gift Card</span>";
                } else if (product.isDigital()) {
                    badge = "<span class='badge badge-digital'>Digital</span>";
                }

                itemsTable.append(String.format("""
                    <tr>
                        <td><strong>%s</strong>%s</td>
                        <td>×%d</td>
                        <td>$%.2f</td>
                        <td><strong>$%.2f</strong></td>
                    </tr>
                    """,
                    product.getName(),
                    badge,
                    item.getQuantity(),
                    item.getPrice(),
                    item.getPrice() * item.getQuantity()
                ));
            }
        }

        itemsTable.append(String.format("""
                </tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="3" style="text-align: right;"><strong>TOTAL:</strong></td>
                        <td><strong>$%.2f</strong></td>
                    </tr>
                </tfoot>
            </table>
            """, order.getTotal()));

        String content = String.format("""
            <div class="header">
                <div class="icon">✅</div>
                <h1>Order Confirmed!</h1>
            </div>
            <div class="content">
                <h2>Thank you for your order, %s!</h2>

                <div class="success-box">
                    <strong>🎉 Your order has been placed successfully!</strong><br>
                    Order #%d • Placed on %s
                </div>

                <h3 style="color: #2c3e50;">Order Details</h3>

                %s

                <div class="info-box">
                    <h4 style="margin: 0 0 10px 0; color: #667eea;">📦 What's Next?</h4>
                    <ul style="margin: 10px 0;">
                        <li>Physical items will be shipped to your address</li>
                        <li>Digital codes have been sent in a separate email</li>
                        <li>You can track your order status in your account</li>
                        <li>You earned loyalty points with this purchase! 💎</li>
                    </ul>
                </div>

                <div style="text-align: center;">
                    <a href="#" class="button">View Order Details</a>
                </div>

                <div class="divider"></div>

                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                    Questions about your order? Contact us at support@shopease.com
                </p>
            </div>
            """,
            user.getUsername(),
            order.getId(),
            dateFormat.format(order.getCreatedAt()),
            itemsTable.toString()
        );

        return EmailSender.sendHtmlEmail(user.getEmail(), subject, wrapInTemplate(content));
    }

    /**
     * Send shipping notification when order is marked as delivered
     */
    public boolean sendShippingNotificationEmail(User user, Order order) {
        String subject = "Your Order #" + order.getId() + " Has Been Delivered! 📦";

        StringBuilder itemsList = new StringBuilder("<ul>");
        for (OrderItem item : order.getItems()) {
            Product product = productDao.getById(item.getProductId()).orElse(null);
            if (product != null) {
                itemsList.append(String.format("<li><strong>%s</strong> (×%d)</li>",
                    product.getName(),
                    item.getQuantity()
                ));
            }
        }
        itemsList.append("</ul>");

        String content = String.format("""
            <div class="header">
                <div class="icon">📦</div>
                <h1>Delivered!</h1>
            </div>
            <div class="content">
                <h2>Great news, %s!</h2>

                <div class="success-box">
                    <strong>✅ Your order has been delivered!</strong><br>
                    Order #%d • Delivered on %s
                </div>

                <h3 style="color: #2c3e50;">Delivered Items</h3>
                %s

                <div class="info-box">
                    <h4 style="margin: 0 0 10px 0; color: #667eea;">💬 Share Your Experience</h4>
                    <p style="margin: 10px 0;">
                        We'd love to hear what you think! Your review helps other gamers make informed decisions.
                    </p>
                </div>

                <div style="text-align: center;">
                    <a href="#" class="button">Leave a Review</a>
                </div>

                <div class="divider"></div>

                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                    Issues with your order? Contact us immediately at support@shopease.com
                </p>
            </div>
            """,
            user.getUsername(),
            order.getId(),
            new SimpleDateFormat("MMM dd, yyyy").format(System.currentTimeMillis()),
            itemsList.toString()
        );

        return EmailSender.sendHtmlEmail(user.getEmail(), subject, wrapInTemplate(content));
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

        String boxClass = order.getStatus().toUpperCase().equals("CANCELLED") ? "warning-box" : "info-box";

        String content = String.format("""
            <div class="header">
                <div class="icon">%s</div>
                <h1>Order Status Update</h1>
            </div>
            <div class="content">
                <h2>Hi %s,</h2>

                <p>Your order status has been updated:</p>

                <div class="%s">
                    <h4 style="margin: 0 0 10px 0;">Order #%d</h4>
                    <p style="margin: 5px 0;"><strong>Previous Status:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>New Status:</strong> %s</p>
                    <p style="margin: 15px 0 0 0;">%s</p>
                </div>

                <div style="text-align: center;">
                    <a href="#" class="button">Track Your Order</a>
                </div>

                <div class="divider"></div>

                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                    Questions? Contact us at support@shopease.com
                </p>
            </div>
            """,
            statusIcon,
            user.getUsername(),
            boxClass,
            order.getId(),
            previousStatus,
            order.getStatus(),
            statusMessage
        );

        return EmailSender.sendHtmlEmail(user.getEmail(), subject, wrapInTemplate(content));
    }

    /**
     * Send promotional email (requires user opt-in)
     */
    public boolean sendPromotionalEmail(User user, String promoTitle, String promoMessage) {
        String subject = "Special Offer from ShopEase - " + promoTitle;

        String content = String.format("""
            <div class="header">
                <div class="icon">🎁</div>
                <h1>%s</h1>
            </div>
            <div class="content">
                <h2>Hi %s,</h2>

                <p style="font-size: 16px; line-height: 1.8;">%s</p>

                <div style="text-align: center;">
                    <a href="#" class="button">Shop Now</a>
                </div>

                <div class="divider"></div>

                <p style="text-align: center; color: #95a5a6; font-size: 12px;">
                    You're receiving this because you opted in to promotional emails.<br>
                    To unsubscribe, please update your preferences in your account settings.
                </p>
            </div>
            """,
            promoTitle,
            user.getUsername(),
            promoMessage.replace("\n", "<br>")
        );

        return EmailSender.sendHtmlEmail(user.getEmail(), subject, wrapInTemplate(content));
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset Request - ShopEase";

        String content = String.format("""
            <div class="header">
                <div class="icon">🔐</div>
                <h1>Password Reset</h1>
            </div>
            <div class="content">
                <h2>Hi %s,</h2>

                <p>We received a request to reset your password for your ShopEase account.</p>

                <div class="code-box">
                    <p style="margin: 0 0 10px 0; color: #6c757d; font-size: 14px;">Your password reset code:</p>
                    <div class="code">%s</div>
                </div>

                <div class="warning-box">
                    <strong>⚠️ Security Notice</strong><br>
                    <ul style="margin: 10px 0;">
                        <li>This code will expire in 24 hours</li>
                        <li>If you didn't request this, please ignore this email</li>
                        <li>Never share this code with anyone</li>
                    </ul>
                </div>

                <div class="divider"></div>

                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                    Need help? Contact us at support@shopease.com
                </p>
            </div>
            """,
            user.getUsername(),
            resetToken
        );

        return EmailSender.sendHtmlEmail(user.getEmail(), subject, wrapInTemplate(content));
    }

    /**
     * Send low stock alert to admin
     */
    public boolean sendLowStockAlertEmail(String adminEmail, Product product) {
        String subject = "⚠️ Low Stock Alert - " + product.getName();

        String content = String.format("""
            <div class="header">
                <div class="icon">⚠️</div>
                <h1>Low Stock Alert</h1>
            </div>
            <div class="content">
                <h2>Inventory Alert</h2>

                <p>The following product is running low on stock:</p>

                <div class="warning-box">
                    <table style="width: 100%%;">
                        <tr>
                            <td style="padding: 5px 0;"><strong>Product:</strong></td>
                            <td style="padding: 5px 0;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 5px 0;"><strong>Category:</strong></td>
                            <td style="padding: 5px 0;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 5px 0;"><strong>Current Stock:</strong></td>
                            <td style="padding: 5px 0; color: #dc3545;"><strong>%d units</strong></td>
                        </tr>
                        <tr>
                            <td style="padding: 5px 0;"><strong>Product ID:</strong></td>
                            <td style="padding: 5px 0;">#%d</td>
                        </tr>
                    </table>
                </div>

                <div class="info-box">
                    <strong>📋 Action Required</strong><br>
                    Please restock this item to avoid running out of inventory.
                </div>

                <div style="text-align: center;">
                    <a href="#" class="button">Manage Inventory</a>
                </div>
            </div>
            """,
            product.getName(),
            product.getCategory(),
            product.getStock(),
            product.getId()
        );

        return EmailSender.sendHtmlEmail(adminEmail, subject, wrapInTemplate(content));
    }
}
