package com.example.finalproject.service;

import com.example.finalproject.dao.DigitalCodeDao;
import com.example.finalproject.model.DigitalCode;

import java.util.Optional;




public class GiftCardService {

    private final DigitalCodeDao digitalCodeDao = new DigitalCodeDao();

    





    public Optional<DigitalCode> validateGiftCard(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }

        
        Optional<DigitalCode> codeOpt = digitalCodeDao.getByCode(code.trim().toUpperCase());

        if (codeOpt.isEmpty()) {
            return Optional.empty();
        }

        DigitalCode digitalCode = codeOpt.get();

        
        if (!"GiftCard".equals(digitalCode.getCodeType())) {
            return Optional.empty();
        }

        
        if (digitalCode.getBalance() <= 0) {
            return Optional.empty();
        }

        return Optional.of(digitalCode);
    }

    





    public double getBalance(String code) {
        Optional<DigitalCode> giftCard = validateGiftCard(code);
        return giftCard.map(DigitalCode::getBalance).orElse(0.0);
    }

    






    public double applyGiftCard(String code, double orderTotal) {
        Optional<DigitalCode> giftCardOpt = validateGiftCard(code);

        if (giftCardOpt.isEmpty() || orderTotal <= 0) {
            return 0.0;
        }

        DigitalCode giftCard = giftCardOpt.get();
        double currentBalance = giftCard.getBalance();

        
        double amountToDeduct = Math.min(currentBalance, orderTotal);

        
        double newBalance = currentBalance - amountToDeduct;
        digitalCodeDao.updateGiftCardBalance(giftCard.getId(), newBalance);

        System.out.println(String.format("✅ Applied gift card %s: -$%.2f (Balance: $%.2f → $%.2f)",
            code, amountToDeduct, currentBalance, newBalance));

        return amountToDeduct;
    }

    





    public boolean isValid(String code) {
        return validateGiftCard(code).isPresent();
    }

    





    public String getGiftCardInfo(String code) {
        Optional<DigitalCode> giftCard = validateGiftCard(code);

        if (giftCard.isEmpty()) {
            return "Invalid or empty gift card";
        }

        DigitalCode gc = giftCard.get();
        return String.format("Gift Card Balance: $%.2f", gc.getBalance());
    }
}
