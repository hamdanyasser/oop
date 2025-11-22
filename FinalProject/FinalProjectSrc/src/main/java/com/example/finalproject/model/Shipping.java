package com.example.finalproject.model;

import java.sql.Timestamp;

public class Shipping {
    private int id;
    private int orderId;
    private String address;
    private String status;
    private Timestamp shippedAt;
    private Timestamp deliveredAt;

    public Shipping() {}

    public Shipping(int id, int orderId, String address, String status,
                    Timestamp shippedAt, Timestamp deliveredAt) {
        this.id = id;
        this.orderId = orderId;
        this.address = address;
        this.status = status;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getShippedAt() { return shippedAt; }
    public void setShippedAt(Timestamp shippedAt) { this.shippedAt = shippedAt; }

    public Timestamp getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Timestamp deliveredAt) { this.deliveredAt = deliveredAt; }
}
