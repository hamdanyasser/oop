package com.example.finalproject.model;

import java.sql.Timestamp;

/**
 * Represents a digital code (gift card or digital download code) sent to a customer
 */
public class DigitalCode {
    private int id;
    private int orderId;
    private int orderItemId;
    private int productId;
    private int userId;
    private String code;
    private String codeType; // GiftCard or DigitalDownload
    private boolean isRedeemed;
    private Timestamp redeemedAt;
    private Timestamp sentAt;
    private Timestamp createdAt;
    private double originalValue; // Original gift card value
    private double balance; // Current gift card balance

    // Constructors
    public DigitalCode() {}

    public DigitalCode(int id, int orderId, int orderItemId, int productId, int userId,
                      String code, String codeType, boolean isRedeemed,
                      Timestamp redeemedAt, Timestamp sentAt, Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.userId = userId;
        this.code = code;
        this.codeType = codeType;
        this.isRedeemed = isRedeemed;
        this.redeemedAt = redeemedAt;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCodeType() { return codeType; }
    public void setCodeType(String codeType) { this.codeType = codeType; }

    public boolean isRedeemed() { return isRedeemed; }
    public void setRedeemed(boolean redeemed) { isRedeemed = redeemed; }

    public Timestamp getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(Timestamp redeemedAt) { this.redeemedAt = redeemedAt; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double getOriginalValue() { return originalValue; }
    public void setOriginalValue(double originalValue) { this.originalValue = originalValue; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
