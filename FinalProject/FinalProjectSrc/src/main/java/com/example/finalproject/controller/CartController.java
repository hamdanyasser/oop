package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.model.Product;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.CartService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

public class CartController {

    private TableView<CartItem> cartTable;
    private TableColumn<CartItem, String> colName;
    private TableColumn<CartItem, Double> colPrice;
    private TableColumn<CartItem, Integer> colQty;
    private TableColumn<CartItem, Double> colTotal;
    private Label totalLabel;
    private Label itemCountLabel;

    private final CartService cartService = CartService.getInstance();

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(1000, 700);
        root.setStyle("-fx-background-color: #f5f7fa;");

        
        HBox topBar = createTopBar();
        root.setTop(topBar);

        
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(30));
        centerBox.setAlignment(Pos.TOP_CENTER);

        VBox tableCard = createTableCard();
        VBox summaryCard = createSummaryCard();

        centerBox.getChildren().addAll(tableCard, summaryCard);

        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(scrollPane);

        
        refreshTable();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("🛒");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("Shopping Cart");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        itemCountLabel = new Label("(0 items)");
        itemCountLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 16px;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = createHeaderButton("🏠 Back to Shop");
        backBtn.setOnAction(e -> onBackToShop());

        Button logoutBtn = createHeaderButton("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(ev -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.5); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(ev -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, itemCountLabel, spacer, backBtn, logoutBtn);

        return topBar;
    }

    private Button createHeaderButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        return btn;
    }

    private VBox createTableCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(900);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Cart Items");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        
        cartTable = new TableView<>();
        cartTable.setPrefHeight(350);
        cartTable.setStyle("-fx-background-color: transparent;");
        cartTable.setPlaceholder(new Label("Your cart is empty"));

        colName = new TableColumn<>("Product Name");
        colName.setPrefWidth(350);
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setCellFactory(col -> new TableCell<CartItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
                }
            }
        });

        colPrice = new TableColumn<>("Unit Price");
        colPrice.setPrefWidth(150);
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        colPrice.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                    setStyle("-fx-text-fill: #667eea; -fx-font-weight: 600;");
                }
            }
        });

        colQty = new TableColumn<>("Quantity");
        colQty.setPrefWidth(120);
        colQty.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());
        colQty.setCellFactory(col -> new TableCell<CartItem, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Label badge = new Label("×" + item);
                    badge.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057; " +
                            "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colTotal = new TableColumn<>("Subtotal");
        colTotal.setPrefWidth(150);
        colTotal.setCellValueFactory(data -> data.getValue().totalProperty().asObject());
        colTotal.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                    setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 14px;");
                }
            }
        });

        cartTable.getColumns().addAll(colName, colPrice, colQty, colTotal);

        
        Button removeBtn = new Button("🗑️ Remove Selected Item");
        removeBtn.setPrefWidth(200);
        removeBtn.setPrefHeight(40);
        removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;"));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;"));
        removeBtn.setOnAction(e -> onRemove());

        HBox btnBox = new HBox(removeBtn);
        btnBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(header, cartTable, btnBox);

        return card;
    }

    private VBox createSummaryCard() {
        VBox card = new VBox(20);
        card.setMaxWidth(900);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        
        VBox totalSection = new VBox(10);
        totalSection.setAlignment(Pos.CENTER);
        totalSection.setPadding(new Insets(20));
        totalSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12;");

        Label totalTextLabel = new Label("Total Amount");
        totalTextLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        totalLabel = new Label("$0.00");
        totalLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: #667eea; -fx-font-weight: bold;");

        totalSection.getChildren().addAll(totalTextLabel, totalLabel);

        
        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER);

        Button continueBtn = new Button("← Continue Shopping");
        continueBtn.setPrefWidth(250);
        continueBtn.setPrefHeight(45);
        continueBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10;");
        continueBtn.setOnMouseEntered(e -> continueBtn.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10;"));
        continueBtn.setOnMouseExited(e -> continueBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10;"));
        continueBtn.setOnAction(e -> onBackToShop());

        Button checkoutBtn = new Button("Proceed to Checkout →");
        checkoutBtn.setPrefWidth(300);
        checkoutBtn.setPrefHeight(50);
        checkoutBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;");
        checkoutBtn.setOnMouseEntered(e -> checkoutBtn.setStyle("-fx-background-color: linear-gradient(to right, #5568d3, #6a3f8f); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;"));
        checkoutBtn.setOnMouseExited(e -> checkoutBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;"));
        checkoutBtn.setOnAction(e -> onCheckout());

        buttonsRow.getChildren().addAll(continueBtn, checkoutBtn);

        card.getChildren().addAll(totalSection, buttonsRow);

        return card;
    }

    private void refreshTable() {
        ObservableList<CartItem> items = FXCollections.observableArrayList();
        int totalItems = 0;

        for (Map.Entry<Product, Integer> e : cartService.getItems().entrySet()) {
            items.add(new CartItem(e.getKey(), e.getValue()));
            totalItems += e.getValue();
        }

        cartTable.setItems(items);
        totalLabel.setText(String.format("$%.2f", cartService.getTotal()));
        itemCountLabel.setText("(" + totalItems + " item" + (totalItems != 1 ? "s" : "") + ")");
    }

    private void onRemove() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Remove Item");
            confirm.setHeaderText("Remove from Cart");
            confirm.setContentText("Remove " + selected.getProduct().getName() + " from your cart?");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                cartService.removeItem(selected.getProduct());
                refreshTable();
                showStyledAlert("Removed", "✅ Item removed from cart", Alert.AlertType.INFORMATION);
            }
        } else {
            showStyledAlert("No Selection", "Please select an item to remove.", Alert.AlertType.WARNING);
        }
    }

    private void onCheckout() {
        if (cartService.getItems().isEmpty()) {
            showStyledAlert("Cart Empty", "Please add items to your cart before checking out.", Alert.AlertType.WARNING);
            return;
        }
        HelloApplication.setRoot(new CheckoutController());
    }

    private void onBackToShop() {
        HelloApplication.setRoot(new CustomerHomeController());
    }

    private void onLogout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }

    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-padding: 20;");

        alert.showAndWait();
    }
}