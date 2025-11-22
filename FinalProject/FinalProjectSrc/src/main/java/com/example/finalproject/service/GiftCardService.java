package com.example.finalproject.service;

import com.example.finalproject.dao.DigitalCodeDao;
import com.example.finalproject.model.DigitalCode;

import java.util.Optional;

/**
 * Service for managing gift card redemption and balance tracking
 */
public class GiftCardService {

    private final DigitalCodeDao digitalCodeDao = new DigitalCodeDao();

    /**
     * Validate and get gift card information
     *
     * @param code The gift card code to validate
     * @return Optional containing the gift card DigitalCode if valid, empty otherwise
     */
    public Optional<DigitalCode> validateGiftCard(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }

        // Get the digital code from database
        Optional<DigitalCode> codeOpt = digitalCodeDao.getByCode(code.trim().toUpperCase());

        if (codeOpt.isEmpty()) {
            return Optional.empty();
        }

        DigitalCode digitalCode = codeOpt.get();

        // Check if it's a gift card
        if (!"GiftCard".equals(digitalCode.getCodeType())) {
            return Optional.empty();
        }

        // Check if it has balance
        if (digitalCode.getBalance() <= 0) {
            return Optional.empty();
        }

        return Optional.of(digitalCode);
    }

    /**
     * Get the current balance of a gift card
     *
     * @param code The gift card code
     * @return The current balance, or 0 if invalid/empty
     */
    public double getBalance(String code) {
        Optional<DigitalCode> giftCard = validateGiftCard(code);
        return giftCard.map(DigitalCode::getBalance).orElse(0.0);
    }

    /**
     * Apply gift card to order and deduct the amount used
     *
     * @param code The gift card code
     * @param orderTotal The total amount of the order
     * @return The amount deducted from the gift card (discount applied)
     */
    public double applyGiftCard(String code, double orderTotal) {
        Optional<DigitalCode> giftCardOpt = validateGiftCard(code);

        if (giftCardOpt.isEmpty() || orderTotal <= 0) {
            return 0.0;
        }

        DigitalCode giftCard = giftCardOpt.get();
        double currentBalance = giftCard.getBalance();

        // Calculate how much to deduct (min of balance and order total)
        double amountToDeduct = Math.min(currentBalance, orderTotal);

        // Update the gift card balance in database
        double newBalance = currentBalance - amountToDeduct;
        digitalCodeDao.updateGiftCardBalance(giftCard.getId(), newBalance);

        System.out.println(String.format("✅ Applied gift card %s: -$%.2f (Balance: $%.2f → $%.2f)",
            code, amountToDeduct, currentBalance, newBalance));

        return amountToDeduct;
    }

    /**
     * Check if a gift card code exists and has balance
     *
     * @param code The gift card code
     * @return true if the gift card is valid and has balance
     */
    public boolean isValid(String code) {
        return validateGiftCard(code).isPresent();
    }

    /**
     * Get formatted gift card info for display
     *
     * @param code The gift card code
     * @return Formatted string with gift card info, or error message
     */
    public String getGiftCardInfo(String code) {
        Optional<DigitalCode> giftCard = validateGiftCard(code);

        if (giftCard.isEmpty()) {
            return "Invalid or empty gift card";
        }

        DigitalCode gc = giftCard.get();
        return String.format("Gift Card Balance: $%.2f", gc.getBalance());
    }
}
