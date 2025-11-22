package com.example.finalproject.controller;

import com.example.finalproject.dao.DBConnection;
import com.example.finalproject.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemsPopupController {

    private int orderId;
    private TableView<OrderItem> itemTable;

    public Parent createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f5f7fa;");
        root.setPrefSize(750, 600);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                "-fx-background-radius: 12;");

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("Order Details");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Table container
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(20));
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label tableTitle = new Label("Order Items");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Create table
        itemTable = new TableView<>();
        itemTable.setPrefHeight(280);

        // Product column
        TableColumn<OrderItem, String> colProduct = new TableColumn<>("Product");
        colProduct.setPrefWidth(250);
        colProduct.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getProductName();
            return new javafx.beans.property.SimpleStringProperty(name != null ? name : "");
        });

        // Quantity column
        TableColumn<OrderItem, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setPrefWidth(100);
        colQty.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject();
        });
        colQty.setStyle("-fx-alignment: CENTER;");

        // Price column
        TableColumn<OrderItem, String> colPrice = new TableColumn<>("Unit Price");
        colPrice.setPrefWidth(120);
        colPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getPrice();
            return new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", price));
        });
        colPrice.setStyle("-fx-alignment: CENTER;");

        // Subtotal column
        TableColumn<OrderItem, String> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setPrefWidth(120);
        colSubtotal.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            double subtotal = item.getPrice() * item.getQuantity();
            return new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", subtotal));
        });
        colSubtotal.setStyle("-fx-alignment: CENTER;");

        itemTable.getColumns().addAll(colProduct, colQty, colPrice, colSubtotal);

        tableBox.getChildren().addAll(tableTitle, itemTable);

        // Summary section
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Status card
        VBox statusCard = new VBox(8);
        statusCard.setAlignment(Pos.CENTER);
        statusCard.setPadding(new Insets(15));
        statusCard.setPrefWidth(280);
        statusCard.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label statusTitle = new Label("Order Status");
        statusTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        Label statusValue = new Label("Loading...");
        statusValue.setId("statusValue");
        statusValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        statusCard.getChildren().addAll(statusTitle, statusValue);

        // Total card
        VBox totalCard = new VBox(8);
        totalCard.setAlignment(Pos.CENTER);
        totalCard.setPadding(new Insets(15));
        totalCard.setPrefWidth(280);
        totalCard.setStyle("-fx-background-color: #667eea20; -fx-background-radius: 10;");

        Label totalTitle = new Label("Total Amount");
        totalTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        Label totalValue = new Label("$0.00");
        totalValue.setId("totalValue");
        totalValue.setStyle("-fx-font-size: 22px; -fx-text-fill: #667eea; -fx-font-weight: bold;");

        totalCard.getChildren().addAll(totalTitle, totalValue);

        summaryBox.getChildren().addAll(statusCard, totalCard);

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setPrefWidth(200);
        closeBtn.setPrefHeight(40);
        closeBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });

        HBox closeBox = new HBox(closeBtn);
        closeBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(header, tableBox, summaryBox, closeBox);

        return root;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadOrderData();
    }

    private void loadOrderData() {
        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;
        String status = "";

        try (Connection conn = DBConnection.getInstance()) {
            // Get order info
            PreparedStatement psOrder = conn.prepareStatement("SELECT total, status FROM orders WHERE id=?");
            psOrder.setInt(1, orderId);
            ResultSet rsOrder = psOrder.executeQuery();
            if (rsOrder.next()) {
                total = rsOrder.getDouble("total");
                status = rsOrder.getString("status");
            }

            // Get order items
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT oi.*, p.name FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?");
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
                item.setProductName(rs.getString("name"));
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update table
        ObservableList<OrderItem> data = FXCollections.observableArrayList(items);
        itemTable.setItems(data);

        // Update summary
        VBox root = (VBox) itemTable.getParent().getParent();
        HBox summaryBox = (HBox) root.getChildren().get(2);

        VBox statusCard = (VBox) summaryBox.getChildren().get(0);
        Label statusValue = (Label) statusCard.lookup("#statusValue");

        VBox totalCard = (VBox) summaryBox.getChildren().get(1);
        Label totalValue = (Label) totalCard.lookup("#totalValue");

        // Update total
        totalValue.setText(String.format("$%.2f", total));

        // Update status with styling
        String upperStatus = status.toUpperCase();
        statusValue.setText(upperStatus);

        switch (upperStatus) {
            case "DELIVERED":
            case "COMPLETED":
                statusValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #28a745; -fx-background-color: #d4edda; " +
                        "-fx-padding: 8 20; -fx-background-radius: 15;");
                break;
            case "PENDING":
                statusValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #856404; -fx-background-color: #fff3cd; " +
                        "-fx-padding: 8 20; -fx-background-radius: 15;");
                break;
            case "CANCELLED":
                statusValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #721c24; -fx-background-color: #f8d7da; " +
                        "-fx-padding: 8 20; -fx-background-radius: 15;");
                break;
            default:
                statusValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #0c5460; -fx-background-color: #d1ecf1; " +
                        "-fx-padding: 8 20; -fx-background-radius: 15;");
        }
    }
}