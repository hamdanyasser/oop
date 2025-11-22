package com.example.finalproject.service;

import com.example.finalproject.dao.DigitalCodeDao;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.dao.UserDao;
import com.example.finalproject.model.DigitalCode;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.User;
import com.example.finalproject.util.EmailSender;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for generating and managing digital codes (gift cards and digital downloads)
 */
public class DigitalCodeService {

    private final DigitalCodeDao digitalCodeDao = new DigitalCodeDao();
    private final ProductDao productDao = new ProductDao();
    private final UserDao userDao = new UserDao();

    /**
     * Generate a unique gift card code
     * Format: GAME-XXXX-XXXX-XXXX
     */
    public String generateGiftCardCode() {
        String code;
        do {
            // Generate code format: GAME-XXXX-XXXX-XXXX
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = String.format("GAME-%s-%s-%s",
                uuid.substring(0, 4),
                uuid.substring(4, 8),
                uuid.substring(8, 12)
            );
        } while (!isCodeUnique(code)); // Ensure uniqueness

        return code;
    }

    /**
     * Generate a unique digital download code
     * Format: DIGI-XXXX-XXXX-XXXX
     */
    public String generateDigitalCode() {
        String code;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = String.format("DIGI-%s-%s-%s",
                uuid.substring(0, 4),
                uuid.substring(4, 8),
                uuid.substring(8, 12)
            );
        } while (!isCodeUnique(code));

        return code;
    }

    /**
     * Check if a code is unique (doesn't exist in database)
     */
    private boolean isCodeUnique(String code) {
        // If code is valid (exists and not redeemed), it's not unique
        // If code doesn't exist, it's unique
        return !digitalCodeDao.isCodeValid(code);
    }

    /**
     * Create and send a digital code for an order item
     *
     * @param orderId The order ID
     * @param orderItemId The order item ID
     * @param productId The product ID
     * @param userId The user ID
     * @param quantity Number of codes to generate
     * @return true if codes were successfully created and sent
     */
    public boolean createAndSendCodes(int orderId, int orderItemId, int productId, int userId, int quantity) {
        try {
            // Get product details
            Optional<Product> productOpt = productDao.getById(productId);
            if (productOpt.isEmpty()) {
                System.err.println("Product not found: " + productId);
                return false;
            }

            Product product = productOpt.get();

            // Get user details
            Optional<User> userOpt = userDao.getUserById(userId);
            if (userOpt.isEmpty()) {
                System.err.println("User not found: " + userId);
                return false;
            }

            User user = userOpt.get();

            // Determine code type
            String codeType = product.isGiftCard() ? "GiftCard" : "DigitalDownload";

            // Generate and save codes
            StringBuilder codesMessage = new StringBuilder();
            for (int i = 0; i < quantity; i++) {
                // Generate code
                String code = product.isGiftCard() ? generateGiftCardCode() : generateDigitalCode();

                // Create DigitalCode object
                DigitalCode digitalCode = new DigitalCode();
                digitalCode.setOrderId(orderId);
                digitalCode.setOrderItemId(orderItemId);
                digitalCode.setProductId(productId);
                digitalCode.setUserId(userId);
                digitalCode.setCode(code);
                digitalCode.setCodeType(codeType);
                digitalCode.setRedeemed(false);
                digitalCode.setSentAt(new Timestamp(System.currentTimeMillis()));

                // Save to database
                digitalCodeDao.insert(digitalCode);

                // Add to email message
                codesMessage.append(code);
                if (i < quantity - 1) {
                    codesMessage.append("\n");
                }
            }

            // Send email to customer
            sendCodeEmail(user.getEmail(), product, codesMessage.toString(), quantity);

            System.out.println("✅ Successfully generated and sent " + quantity + " code(s) for product: " + product.getName());
            return true;

        } catch (Exception e) {
            System.err.println("Error creating and sending codes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send email with digital code(s)
     */
    private void sendCodeEmail(String email, Product product, String codes, int quantity) {
        try {
            String subject;
            String body;

            if (product.isGiftCard()) {
                // Gift card email
                subject = "Your Gaming Gift Card Code" + (quantity > 1 ? "s" : "");

                body = String.format("""
                    Thank you for your purchase!

                    Your %s Gift Card Code%s:

                    %s

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    HOW TO REDEEM:
                    1. Log in to your account
                    2. Go to checkout
                    3. Enter your gift card code
                    4. The amount will be applied to your order

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    This code can be used by you or shared as a gift!
                    Store this email safely - codes cannot be resent.

                    Happy Gaming!
                    ShopEase Gaming Store
                    """,
                    product.getName(),
                    quantity > 1 ? "s" : "",
                    codes
                );
            } else {
                // Digital download email
                subject = "Your Digital Download Code" + (quantity > 1 ? "s" : "") + " - " + product.getName();

                body = String.format("""
                    Thank you for your purchase!

                    Your digital download code%s for %s:

                    %s

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    HOW TO DOWNLOAD:
                    1. Log in to your account
                    2. Go to "My Digital Codes"
                    3. Use your code to access the download

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    Store this email safely - codes cannot be resent.

                    Happy Gaming!
                    ShopEase Gaming Store
                    """,
                    quantity > 1 ? "s" : "",
                    product.getName(),
                    codes
                );
            }

            // Send email
            EmailSender.sendEmail(email, subject, body);
            System.out.println("📧 Code email sent to: " + email);

        } catch (Exception e) {
            System.err.println("Error sending code email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all codes for a user
     */
    public java.util.List<DigitalCode> getUserCodes(int userId) {
        return digitalCodeDao.getCodesByUserId(userId);
    }

    /**
     * Redeem a code
     */
    public boolean redeemCode(String code) {
        if (digitalCodeDao.isCodeValid(code)) {
            digitalCodeDao.redeemCode(code);
            return true;
        }
        return false;
    }
}
