package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.OrderDao;
import com.example.finalproject.model.Order;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class OrderHistoryController {

    private TableView<Order> orderTable;
    private TableColumn<Order, Integer> colId;
    private TableColumn<Order, Double> colTotal;
    private TableColumn<Order, String> colStatus;
    private TableColumn<Order, Timestamp> colDate;
    private Label emptyLabel;
    private HBox statsCard;

    private final OrderDao orderDao = new OrderDao();

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(900, 650);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar with gradient
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center content
        StackPane centerStack = new StackPane();

        VBox centerContent = createCenterContent();

        // Empty state label
        emptyLabel = new Label("📦 No orders yet\nStart shopping to see your orders here!");
        emptyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #6c757d; -fx-text-alignment: center;");
        emptyLabel.setVisible(false);

        centerStack.getChildren().addAll(centerContent, emptyLabel);
        root.setCenter(centerStack);

        // Initialize data
        loadOrders();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setSpacing(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-padding: 20;");

        // Icon and Title
        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("My Orders");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Shop");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        backBtn.setOnAction(e -> onBack());

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.9); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.9); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn, logoutBtn);

        return topBar;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox();
        centerContent.setSpacing(20);
        centerContent.setAlignment(Pos.TOP_CENTER);
        centerContent.setPadding(new Insets(30));

        // Stats card
        HBox statsCard = createStatsCard();

        // Table container with modern styling
        VBox tableContainer = new VBox(15);
        tableContainer.setAlignment(Pos.CENTER);
        tableContainer.setPadding(new Insets(25));
        tableContainer.setMaxWidth(850);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Table header
        Label tableTitle = new Label("📋 Order History");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Create table
        orderTable = new TableView<>();
        orderTable.setPrefHeight(400);
        orderTable.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");

        // Create columns with modern styling
        colId = new TableColumn<>("Order #");
        colId.setPrefWidth(100);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER;");

        colTotal = new TableColumn<>("Total Amount");
        colTotal.setPrefWidth(150);
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("$" + String.format("%.2f", item));
                    setStyle("-fx-text-fill: #667eea; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(200);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String badgeStyle = "-fx-background-radius: 12; -fx-padding: 5 15; " +
                            "-fx-font-weight: 600; -fx-font-size: 12px; -fx-alignment: CENTER;";

                    switch (item.toLowerCase()) {
                        case "pending":
                            setStyle(badgeStyle + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "completed":
                        case "delivered":
                            setStyle(badgeStyle + "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "cancelled":
                            setStyle(badgeStyle + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle(badgeStyle + "-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;");
                    }
                }
            }
        });

        colDate = new TableColumn<>("Order Date");
        colDate.setPrefWidth(250);
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDate.setStyle("-fx-alignment: CENTER;");

        orderTable.getColumns().addAll(colId, colTotal, colStatus, colDate);

        // Action buttons
        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER);
        buttonsRow.setPadding(new Insets(10, 0, 0, 0));

        Button viewItemsBtn = new Button("👁️ View Order Details");
        viewItemsBtn.setPrefWidth(200);
        viewItemsBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;");
        viewItemsBtn.setOnMouseEntered(e -> viewItemsBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 4);"));
        viewItemsBtn.setOnMouseExited(e -> viewItemsBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;"));
        viewItemsBtn.setOnAction(e -> onViewItems());

        buttonsRow.getChildren().add(viewItemsBtn);

        tableContainer.getChildren().addAll(tableTitle, orderTable, buttonsRow);
        centerContent.getChildren().addAll(statsCard, tableContainer);

        return centerContent;
    }

    private HBox createStatsCard() {
        statsCard = new HBox(30);
        statsCard.setAlignment(Pos.CENTER);
        statsCard.setPadding(new Insets(20));
        statsCard.setMaxWidth(850);
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // These will be populated in loadOrders
        VBox totalOrders = createStatBox("📦", "0", "Total Orders", "#667eea");
        VBox totalSpent = createStatBox("💰", "$0.00", "Total Spent", "#28a745");
        VBox pendingOrders = createStatBox("⏳", "0", "Pending", "#ffc107");

        statsCard.getChildren().addAll(totalOrders, totalSpent, pendingOrders);

        return statsCard;
    }

    private VBox createStatBox(String icon, String value, String label, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "40; -fx-border-radius: 12; -fx-border-width: 1;");
        box.setPrefWidth(220);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        box.getChildren().addAll(iconLabel, valueLabel, textLabel);

        return box;
    }

    private void loadOrders() {
        int userId = JwtService.getUserId(Session.getToken());
        List<Order> allOrders = orderDao.findAll();

        // Filter only this user's orders
        List<Order> userOrders = allOrders.stream()
                .filter(o -> o.getUserId() == userId)
                .collect(Collectors.toList());

        if (userOrders.isEmpty()) {
            emptyLabel.setVisible(true);
            return;
        }

        ObservableList<Order> orders = FXCollections.observableArrayList(userOrders);
        orderTable.setItems(orders);

        // Update stats
        updateStats(userOrders);
    }

    private void updateStats(List<Order> orders) {
        int totalCount = orders.size();
        double totalAmount = orders.stream().mapToDouble(Order::getTotal).sum();
        long pendingCount = orders.stream()
                .filter(o -> "pending".equalsIgnoreCase(o.getStatus()))
                .count();

        // Update the stats card directly using the instance variable
        if (statsCard != null && statsCard.getChildren().size() == 3) {
            ((Label) ((VBox) statsCard.getChildren().get(0)).getChildren().get(1)).setText(String.valueOf(totalCount));
            ((Label) ((VBox) statsCard.getChildren().get(1)).getChildren().get(1)).setText("$" + String.format("%.2f", totalAmount));
            ((Label) ((VBox) statsCard.getChildren().get(2)).getChildren().get(1)).setText(String.valueOf(pendingCount));
        }
    }

    private void onBack() {
        HelloApplication.setRoot(new CustomerHomeController());
    }

    private void onViewItems() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "⚠️ Please select an order to view its details.", Alert.AlertType.WARNING);
            return;
        }

        try {
            OrderItemsPopupController controller = new OrderItemsPopupController();
            Parent root = controller.createView();
            controller.setOrderId(selected.getId());

            Stage stage = new Stage();
            stage.setTitle("Order Details - #" + selected.getId());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Error", "❌ Failed to load order details.", Alert.AlertType.ERROR);
        }
    }

    private void onLogout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }

    private void showStyledAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-padding: 20;");

        alert.showAndWait();
    }
}