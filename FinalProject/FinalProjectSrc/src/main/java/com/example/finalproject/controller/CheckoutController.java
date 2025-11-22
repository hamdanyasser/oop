package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.OrderDao;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.dao.UserDao;
import com.example.finalproject.model.Order;
import com.example.finalproject.model.OrderItem;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.User;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.CartService;
import com.example.finalproject.service.DigitalCodeService;
import com.example.finalproject.service.EmailNotificationService;
import com.example.finalproject.service.LoyaltyService;
import com.example.finalproject.service.GiftCardService;
import com.example.finalproject.util.ToastNotification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckoutController {

    private Label totalLabel;
    private VBox itemsList;
    private CheckBox usePointsCheckbox;
    private Slider pointsSlider;
    private Label pointsDiscountLabel;
    private int pointsToRedeem = 0;
    private double originalTotal = 0;

    // Gift card fields
    private TextField giftCardField;
    private Label giftCardBalanceLabel;
    private Button removeGiftCardBtn;
    private String appliedGiftCardCode = null;
    private double giftCardBalance = 0; // Store balance, not discount (we recalculate discount dynamically)

    private final CartService cartService = CartService.getInstance();
    private final OrderDao orderDao = new OrderDao();
    private final ProductDao productDao = new ProductDao();
    private final UserDao userDao = new UserDao();
    private final DigitalCodeService digitalCodeService = new DigitalCodeService();
    private final EmailNotificationService emailNotificationService = new EmailNotificationService();
    private final LoyaltyService loyaltyService = new LoyaltyService();
    private final GiftCardService giftCardService = new GiftCardService();

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(900, 700);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(40));

        // Order summary card
        VBox summaryCard = createSummaryCard();

        centerBox.getChildren().add(summaryCard);
        scrollPane.setContent(centerBox);
        root.setCenter(scrollPane);

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("🛒");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("Checkout");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Cart");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        backBtn.setOnAction(e -> onBack());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn);
        return topBar;
    }

    private VBox createSummaryCard() {
        VBox card = new VBox(25);
        card.setMaxWidth(600);
        card.setPadding(new Insets(35));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label titleLabel = new Label("Order Summary");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Items list
        itemsList = new VBox(10);
        itemsList.setPadding(new Insets(15, 0, 15, 0));

        Map<Product, Integer> cartItems = cartService.getItems();
        if (cartItems.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
            itemsList.getChildren().add(emptyLabel);
        } else {
            for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                itemsList.getChildren().add(createItemRow(entry.getKey(), entry.getValue()));
            }
        }

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Loyalty points redemption section
        VBox redemptionSection = createRedemptionSection();

        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(10, 0, 10, 0));

        // Gift card redemption section
        VBox giftCardSection = createGiftCardSection();

        Separator separator3 = new Separator();
        separator3.setPadding(new Insets(10, 0, 10, 0));

        // Total section
        originalTotal = cartService.getTotal();
        HBox totalBox = new HBox();
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(15, 0, 0, 0));

        Label totalTextLabel = new Label("Total Amount:");
        totalTextLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        totalLabel = new Label(String.format("$%.2f", originalTotal));
        totalLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #667eea; -fx-font-weight: bold; -fx-padding: 0 0 0 15;");

        totalBox.getChildren().addAll(totalTextLabel, totalLabel);

        // Buttons
        VBox buttonBox = new VBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button confirmBtn = new Button("✅ Confirm & Place Order");
        confirmBtn.setPrefWidth(400);
        confirmBtn.setPrefHeight(50);
        confirmBtn.setStyle("-fx-background-color: linear-gradient(to right, #28a745, #20c997); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;");
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle("-fx-background-color: linear-gradient(to right, #218838, #1ba87f); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;"));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle("-fx-background-color: linear-gradient(to right, #28a745, #20c997); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;"));
        confirmBtn.setOnAction(e -> onConfirmOrder());

        Button backBtn = new Button("← Continue Shopping");
        backBtn.setPrefWidth(400);
        backBtn.setPrefHeight(45);
        backBtn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #495057; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10; " +
                "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;");
        backBtn.setOnAction(e -> HelloApplication.setRoot(new CustomerHomeController()));

        buttonBox.getChildren().addAll(confirmBtn, backBtn);

        card.getChildren().addAll(header, itemsList, separator, redemptionSection, separator2, giftCardSection, separator3, totalBox, buttonBox);
        return card;
    }

    private HBox createItemRow(Product product, int quantity) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");
        nameLabel.setPrefWidth(250);

        Label qtyLabel = new Label("×" + quantity);
        qtyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        qtyLabel.setPrefWidth(60);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double itemTotal = product.getEffectivePrice() * quantity;
        Label priceLabel = new Label(String.format("$%.2f", itemTotal));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #667eea;");

        row.getChildren().addAll(nameLabel, qtyLabel, spacer, priceLabel);
        return row;
    }

    private void onConfirmOrder() {
        try {
            int userId = JwtService.getUserId(Session.getToken());
            Map<Product, Integer> cartItems = cartService.getItems();

            if (cartItems.isEmpty()) {
                showStyledAlert("Empty Cart", "Your cart is empty!", Alert.AlertType.WARNING);
                return;
            }

            // Validate stock availability before placing order
            for (var entry : cartItems.entrySet()) {
                Product p = entry.getKey();
                int requestedQty = entry.getValue();

                // Get fresh product data from database to check current stock
                Product freshProduct = productDao.getById(p.getId()).orElse(null);
                if (freshProduct == null) {
                    showStyledAlert("Product Not Found",
                        "Product '" + p.getName() + "' no longer exists!",
                        Alert.AlertType.ERROR);
                    return;
                }

                if (freshProduct.getStock() < requestedQty) {
                    showStyledAlert("Insufficient Stock",
                        String.format("Only %d units of '%s' available (you requested %d)",
                            freshProduct.getStock(), p.getName(), requestedQty),
                        Alert.AlertType.WARNING);
                    return;
                }
            }

            List<OrderItem> orderItems = new ArrayList<>();
            for (var entry : cartItems.entrySet()) {
                Product p = entry.getKey();
                int qty = entry.getValue();
                double unit = p.getEffectivePrice();
                orderItems.add(new OrderItem(0, 0, p.getId(), qty, unit));
            }

            // Calculate final total with points redemption
            double finalTotal = originalTotal;
            if (pointsToRedeem > 0) {
                double discount = loyaltyService.calculateDiscount(pointsToRedeem);
                finalTotal = Math.max(0, originalTotal - discount);
            }

            // Apply gift card if used (calculate actual amount to deduct)
            double actualGiftCardDeduction = 0;
            if (appliedGiftCardCode != null && !appliedGiftCardCode.isEmpty() && giftCardBalance > 0) {
                // Calculate how much to actually deduct (min of balance and remaining total)
                double amountToDeduct = Math.min(giftCardBalance, finalTotal);
                actualGiftCardDeduction = giftCardService.applyGiftCard(appliedGiftCardCode, amountToDeduct);
                finalTotal = Math.max(0, finalTotal - actualGiftCardDeduction);
                System.out.println("✅ Applied gift card " + appliedGiftCardCode + ": -$" +
                    String.format("%.2f", actualGiftCardDeduction));
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setItems(orderItems);
            order.setTotal(finalTotal);
            order.setStatus("PENDING");
            orderDao.saveOrder(order);

            // Deduct stock for each product in the order
            for (var entry : cartItems.entrySet()) {
                Product p = entry.getKey();
                int qty = entry.getValue();

                // Get fresh product and update stock
                Product freshProduct = productDao.getById(p.getId()).orElse(null);
                if (freshProduct != null) {
                    int newStock = freshProduct.getStock() - qty;
                    freshProduct.setStock(Math.max(0, newStock)); // Ensure stock doesn't go negative
                    productDao.update(freshProduct);
                    System.out.println(String.format("✅ Updated stock for %s: %d → %d",
                        freshProduct.getName(), freshProduct.getStock() + qty, newStock));
                }
            }

            // Redeem points if used
            if (pointsToRedeem > 0) {
                loyaltyService.redeemPoints(userId, pointsToRedeem, order.getId());
                System.out.println("✅ Redeemed " + pointsToRedeem + " loyalty points for $" +
                    String.format("%.2f", loyaltyService.calculateDiscount(pointsToRedeem)) + " discount");
            }

            // Generate and send digital codes for gift cards and digital products
            boolean hasDigitalItems = false;
            for (OrderItem item : order.getItems()) {
                Product product = productDao.getById(item.getProductId()).orElse(null);
                if (product != null && product.isDigital()) {
                    hasDigitalItems = true;
                    digitalCodeService.createAndSendCodes(
                        order.getId(),
                        item.getId(),
                        product.getId(),
                        userId,
                        item.getQuantity()
                    );
                    System.out.println("✅ Generated and sent " + item.getQuantity() + " code(s) for: " + product.getName());
                }
            }

            // Send order confirmation email
            User user = userDao.getUserById(userId).orElse(null);
            if (user != null) {
                emailNotificationService.sendOrderConfirmationEmail(user, order);
                System.out.println("✅ Order confirmation email sent to: " + user.getEmail());
            }

            // Award loyalty points
            int pointsEarned = loyaltyService.awardPoints(userId, order.getTotal(), order.getId());
            System.out.println("✅ Awarded " + pointsEarned + " loyalty points to user " + userId);

            cartService.clear();

            String successMessage = hasDigitalItems
                ? "Order placed! Digital codes sent to your email."
                : "Order placed successfully!";
            ToastNotification.success(successMessage);

            // Show points earned notification
            if (pointsEarned > 0) {
                ToastNotification.info(String.format("You earned %d loyalty points! 💎", pointsEarned));
            }

            HelloApplication.setRoot(new CustomerHomeController());
        } catch (SQLException e) {
            e.printStackTrace();
            ToastNotification.error("Error saving order: " + e.getMessage());
        }
    }

    private void onBack() {
        HelloApplication.setRoot(new CartController());
    }

    private VBox createRedemptionSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f0f3ff; -fx-background-radius: 10; " +
                "-fx-border-color: #667eea; -fx-border-width: 1; -fx-border-radius: 10;");

        // Get user's points
        int userId = JwtService.getUserId(Session.getToken());
        int availablePoints = loyaltyService.getPoints(userId);
        double maxDiscount = loyaltyService.calculateDiscount(availablePoints);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("💎");
        icon.setStyle("-fx-font-size: 20px;");
        Label title = new Label("Use Loyalty Points");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        header.getChildren().addAll(icon, title);

        // Points balance display
        Label balanceLabel = new Label(String.format("Available: %,d points (up to $%.2f discount)", availablePoints, maxDiscount));
        balanceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        // Checkbox to enable redemption
        usePointsCheckbox = new CheckBox("I want to use my points for this order");
        usePointsCheckbox.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Slider for point selection
        pointsSlider = new Slider(0, availablePoints, 0);
        pointsSlider.setBlockIncrement(100);
        pointsSlider.setMajorTickUnit(Math.max(500, availablePoints / 4));
        pointsSlider.setShowTickMarks(true);
        pointsSlider.setShowTickLabels(true);
        pointsSlider.setDisable(true);

        // Discount label
        pointsDiscountLabel = new Label("Discount: $0.00");
        pointsDiscountLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        // Event listeners
        usePointsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            pointsSlider.setDisable(!newVal);
            if (!newVal) {
                pointsSlider.setValue(0);
                updateTotal();
            }
        });

        pointsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            pointsToRedeem = newVal.intValue();
            double discount = loyaltyService.calculateDiscount(pointsToRedeem);
            pointsDiscountLabel.setText(String.format("Discount: -$%.2f", discount));
            updateTotal();
        });

        section.getChildren().addAll(header, balanceLabel, usePointsCheckbox, pointsSlider, pointsDiscountLabel);
        return section;
    }

    private VBox createGiftCardSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #fff9e6; -fx-background-radius: 10; " +
                "-fx-border-color: #ffc107; -fx-border-width: 1; -fx-border-radius: 10;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("🎁");
        icon.setStyle("-fx-font-size: 20px;");
        Label title = new Label("Apply Gift Card");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffc107;");
        header.getChildren().addAll(icon, title);

        // Input field and buttons
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        giftCardField = new TextField();
        giftCardField.setPromptText("Enter gift card code (e.g., GAME-XXXX-XXXX-XXXX)");
        giftCardField.setPrefWidth(300);
        giftCardField.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 8; " +
                "-fx-border-color: #dee2e6; -fx-border-radius: 8;");

        Button applyBtn = new Button("Apply");
        applyBtn.setPrefWidth(80);
        applyBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;");
        applyBtn.setOnMouseEntered(e -> applyBtn.setStyle("-fx-background-color: #e0a800; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;"));
        applyBtn.setOnMouseExited(e -> applyBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;"));
        applyBtn.setOnAction(e -> onApplyGiftCard());

        removeGiftCardBtn = new Button("Remove");
        removeGiftCardBtn.setPrefWidth(80);
        removeGiftCardBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;");
        removeGiftCardBtn.setOnMouseEntered(e -> removeGiftCardBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;"));
        removeGiftCardBtn.setOnMouseExited(e -> removeGiftCardBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;"));
        removeGiftCardBtn.setOnAction(e -> onRemoveGiftCard());
        removeGiftCardBtn.setVisible(false);

        inputBox.getChildren().addAll(giftCardField, applyBtn, removeGiftCardBtn);

        // Balance/status label
        giftCardBalanceLabel = new Label("");
        giftCardBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        section.getChildren().addAll(header, inputBox, giftCardBalanceLabel);
        return section;
    }

    private void onApplyGiftCard() {
        String code = giftCardField.getText();

        if (code == null || code.trim().isEmpty()) {
            giftCardBalanceLabel.setText("⚠️ Please enter a gift card code");
            giftCardBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return;
        }

        // Validate gift card
        if (!giftCardService.isValid(code.trim().toUpperCase())) {
            giftCardBalanceLabel.setText("❌ Invalid or empty gift card code");
            giftCardBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
            appliedGiftCardCode = null;
            giftCardBalance = 0;
            updateTotal();
            return;
        }

        // Get gift card info and balance
        double balance = giftCardService.getBalance(code.trim().toUpperCase());

        // Apply the gift card
        appliedGiftCardCode = code.trim().toUpperCase();
        giftCardBalance = balance;

        // Update UI
        giftCardField.setDisable(true);
        removeGiftCardBtn.setVisible(true);
        updateGiftCardLabel();
        updateTotal();

        ToastNotification.success("Gift card applied successfully!");
    }

    private void onRemoveGiftCard() {
        appliedGiftCardCode = null;
        giftCardBalance = 0;
        giftCardField.setText("");
        giftCardField.setDisable(false);
        removeGiftCardBtn.setVisible(false);
        giftCardBalanceLabel.setText("");
        updateTotal();
        ToastNotification.info("Gift card removed");
    }

    private void updateGiftCardLabel() {
        if (appliedGiftCardCode != null && giftCardBalance > 0) {
            double currentTotal = originalTotal;
            if (pointsToRedeem > 0) {
                double pointsDiscount = loyaltyService.calculateDiscount(pointsToRedeem);
                currentTotal = Math.max(0, originalTotal - pointsDiscount);
            }
            double willApply = Math.min(giftCardBalance, currentTotal);
            giftCardBalanceLabel.setText(String.format("✅ Balance: $%.2f | Applying: $%.2f to your order",
                giftCardBalance, willApply));
            giftCardBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        }
    }

    private void updateTotal() {
        double total = originalTotal;

        // Apply loyalty points discount
        double pointsDiscount = 0;
        if (pointsToRedeem > 0) {
            pointsDiscount = loyaltyService.calculateDiscount(pointsToRedeem);
            total = Math.max(0, total - pointsDiscount);
        }

        // Apply gift card discount (dynamically calculated)
        double giftCardDiscount = 0;
        if (appliedGiftCardCode != null && giftCardBalance > 0) {
            giftCardDiscount = Math.min(giftCardBalance, total);
            total = Math.max(0, total - giftCardDiscount);
        }

        // Update gift card label if applied (to reflect new discount amount)
        if (appliedGiftCardCode != null) {
            updateGiftCardLabel();
        }

        // Update total label with breakdown if discounts applied
        if (pointsDiscount > 0 || giftCardDiscount > 0) {
            StringBuilder breakdown = new StringBuilder();
            breakdown.append(String.format("$%.2f", total));
            breakdown.append(String.format("\n(Subtotal: $%.2f", originalTotal));
            if (pointsDiscount > 0) {
                breakdown.append(String.format(" - Points: $%.2f", pointsDiscount));
            }
            if (giftCardDiscount > 0) {
                breakdown.append(String.format(" - Gift: $%.2f", giftCardDiscount));
            }
            breakdown.append(")");
            totalLabel.setText(breakdown.toString());
        } else {
            totalLabel.setText(String.format("$%.2f", originalTotal));
        }
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