package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.dao.WishlistDao;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.ShoppingCart;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class WishlistController {

    private FlowPane wishlistGrid;
    private Label emptyLabel;

    private final WishlistDao wishlistDao = new WishlistDao();
    private final ProductDao productDao = new ProductDao();
    private final CartService cartService = CartService.getInstance();

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(900, 650);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar with gradient
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center with scroll
        StackPane centerStack = new StackPane();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        wishlistGrid = new FlowPane();
        wishlistGrid.setHgap(20);
        wishlistGrid.setVgap(20);
        wishlistGrid.setAlignment(Pos.TOP_CENTER);
        wishlistGrid.setPadding(new Insets(30));
        wishlistGrid.setStyle("-fx-background-color: transparent;");

        scrollPane.setContent(wishlistGrid);

        // Empty state label
        emptyLabel = new Label("★ Your wishlist is empty\nAdd products you love!");
        emptyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #6c757d; -fx-text-alignment: center;");
        emptyLabel.setVisible(false);

        centerStack.getChildren().addAll(scrollPane, emptyLabel);
        root.setCenter(centerStack);

        loadWishlist();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setSpacing(15);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-padding: 20;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Icon and Title
        Label iconLabel = new Label("★");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("My Wishlist");
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

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn);

        return topBar;
    }

    private void loadWishlist() {
        wishlistGrid.getChildren().clear();
        List<Product> products = wishlistDao.getUserWishlist(Session.getUserId());

        if (products.isEmpty()) {
            emptyLabel.setVisible(true);
            return;
        } else {
            emptyLabel.setVisible(false);
        }

        for (Product p : products) {
            VBox card = createProductCard(p);
            wishlistGrid.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        card.setPrefWidth(220);
        card.setMaxWidth(220);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #667eea; -fx-border-radius: 16; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 20, 0, 0, 8); " +
                "-fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"));

        // Image container
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(140, 140);
        imgContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12;");

        ImageView image = new ImageView(new Image("file:" + p.getImagePath(), 130, 130, true, true));
        image.setPreserveRatio(true);
        imgContainer.getChildren().add(image);

        // Product name
        Label name = new Label(p.getName());
        name.setWrapText(true);
        name.setMaxWidth(200);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");

        // Price with styling
        Label price = new Label("$" + String.format("%.2f", p.getPrice()));
        price.setStyle("-fx-font-size: 18px; -fx-text-fill: #667eea; -fx-font-weight: bold;");

        // Stock badge
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER);
        Label stockIcon = new Label("📦");
        Label stock = new Label("Stock: " + p.getStock());
        stock.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        stockBox.getChildren().addAll(stockIcon, stock);

        // Quantity section
        HBox qtyBox = new HBox(8);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setPadding(new Insets(8, 0, 0, 0));

        Label qtyLabel = new Label("Qty:");
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057;");

        Spinner<Integer> qtySpinner = new Spinner<>();
        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, p.getStock(), 1));
        qtySpinner.setPrefWidth(85);
        qtySpinner.setStyle("-fx-background-radius: 8;");

        qtyBox.getChildren().addAll(qtyLabel, qtySpinner);

        // Separator
        Separator sep = new Separator();
        sep.setPadding(new Insets(5, 0, 5, 0));

        // Add to Cart button
        Button addToCart = new Button("🛒 Add to Cart");
        addToCart.setPrefWidth(180);
        addToCart.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-font-size: 13px;");
        addToCart.setOnMouseEntered(e -> addToCart.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-font-size: 13px;"));
        addToCart.setOnMouseExited(e -> addToCart.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-font-size: 13px;"));
        addToCart.setOnAction(e -> {
            int qty = qtySpinner.getValue();

            if (p.getStock() <= 0) {
                showStyledAlert("Out of Stock", "❌ " + p.getName() + " is currently unavailable.", Alert.AlertType.ERROR);
                return;
            }

            if (qty > p.getStock()) {
                showStyledAlert("Insufficient Stock", "⚠️ Only " + p.getStock() + " items available for " + p.getName(), Alert.AlertType.WARNING);
                return;
            }

            cartService.addItem(p, qty);
            ShoppingCart.addItem(p.getId(), p.getEffectivePrice(), qty, p.getStock());

            productDao.decreaseStock(p.getId(), qty);
            p.setStock(p.getStock() - qty);
            stock.setText("Stock: " + p.getStock());

            wishlistDao.removeFromWishlist(Session.getUserId(), p.getId());
            loadWishlist();

            showStyledAlert("Added to Cart", "✅ " + qty + " × " + p.getName() + " added successfully!", Alert.AlertType.INFORMATION);
        });

        // Remove button
        Button remove = new Button("🗑️ Remove");
        remove.setPrefWidth(180);
        remove.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #dc3545; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-font-weight: 600; " +
                "-fx-font-size: 13px; -fx-border-color: #dc3545; -fx-border-width: 1; -fx-border-radius: 10;");
        remove.setOnMouseEntered(e -> remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-font-weight: 600; " +
                "-fx-font-size: 13px; -fx-border-color: #dc3545; -fx-border-width: 1; -fx-border-radius: 10;"));
        remove.setOnMouseExited(e -> remove.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #dc3545; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-font-weight: 600; " +
                "-fx-font-size: 13px; -fx-border-color: #dc3545; -fx-border-width: 1; -fx-border-radius: 10;"));
        remove.setOnAction(e -> {
            wishlistDao.removeFromWishlist(Session.getUserId(), p.getId());
            loadWishlist();
        });

        card.getChildren().addAll(
                imgContainer,
                name,
                price,
                stockBox,
                qtyBox,
                sep,
                addToCart,
                remove
        );

        return card;
    }

    private void onBack() {
        HelloApplication.setRoot(new CustomerHomeController());
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