package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.dao.WishlistDao;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.Platform;
import com.example.finalproject.model.Genre;
import com.example.finalproject.model.ShoppingCart;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.ProductService;
import com.example.finalproject.service.CartService;
import com.example.finalproject.service.ReviewService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class CustomerHomeController {

    private FlowPane productGrid;
    private final ProductService productService = new ProductService();
    private final CartService cartService = CartService.getInstance();
    private final ProductDao productDao = new ProductDao();
    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 12;
    private List<Product> allProducts;
    private Button prevBtn, nextBtn;
    private Label pageLabel;
    private TextField searchField;
    private ComboBox<String> categoryChoice;
    private ComboBox<String> platformChoice;
    private ComboBox<String> genreChoice;
    private List<Platform> allPlatforms;
    private List<Genre> allGenres;
    private List<Product> filteredProducts;
    private ComboBox<String> sortChoice;

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(1200, 750);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar
        VBox topSection = new VBox();
        topSection.getChildren().addAll(createTopBar(), createFilterBar(), createSortBar());

        root.setTop(topSection);

        // Center - Product Grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        productGrid = new FlowPane();
        productGrid.setHgap(25);
        productGrid.setVgap(25);
        productGrid.setAlignment(Pos.CENTER);
        productGrid.setPadding(new Insets(30));
        productGrid.setStyle("-fx-background-color: transparent;");

        scrollPane.setContent(productGrid);
        root.setCenter(scrollPane);

        // Bottom - Pagination
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);

        // Initialize
        loadProducts();
        loadPlatforms();
        loadGenres();
        categoryChoice.getItems().addAll("All", "Console", "PC", "Accessory", "Game", "Controller");
        categoryChoice.setValue("All");

        // Listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        platformChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        genreChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        return root;
    }
    private HBox createSortBar() {
        HBox sortBar = new HBox(15);
        sortBar.setAlignment(Pos.CENTER_LEFT);
        sortBar.setPadding(new Insets(5, 30, 10, 30));
        sortBar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        Label sortLabel = new Label("Sort By:");
        sortLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        sortChoice = new ComboBox<>();
        sortChoice.setPrefWidth(200);
        sortChoice.getItems().addAll(
                "None",
                "Price: Low → High",
                "Price: High → Low",
                "Newest → Oldest",
                "Oldest → Newest"
        );

        sortChoice.setValue("None");
        sortChoice.setStyle("-fx-background-radius: 10; -fx-font-size: 14px;");

        sortChoice.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> applyFilters());

        sortBar.getChildren().addAll(sortLabel, sortChoice);

        return sortBar;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("🎮");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("ShopEase");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: white; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cartBtn = createHeaderButton("🛒 Cart");
        cartBtn.setOnAction(e -> onViewCart());

        Button wishlistBtn = createHeaderButton("❤️ Wishlist");
        wishlistBtn.setOnAction(e -> onWishlist());

        Button ordersBtn = createHeaderButton("📦 Orders");
        ordersBtn.setOnAction(e -> onViewOrders());

        Button profileBtn = createHeaderButton("👤 Profile");
        profileBtn.setOnAction(e -> onProfile());

        Button logoutBtn = createHeaderButton("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.5); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, cartBtn, wishlistBtn, ordersBtn, profileBtn, logoutBtn);
        return topBar;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15, 30, 15, 30));
        filterBar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");

        searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-padding: 10 15; -fx-font-size: 14px;");

        Label filterIcon = new Label("🏷️");
        filterIcon.setStyle("-fx-font-size: 18px;");

        categoryChoice = new ComboBox<>();
        categoryChoice.setPrefWidth(180);
        categoryChoice.setStyle("-fx-background-radius: 10; -fx-font-size: 14px;");

        Label platformIcon = new Label("🎮");
        platformIcon.setStyle("-fx-font-size: 18px;");

        platformChoice = new ComboBox<>();
        platformChoice.setPrefWidth(200);
        platformChoice.setPromptText("All Platforms");
        platformChoice.setStyle("-fx-background-radius: 10; -fx-font-size: 14px;");

        Label genreIcon = new Label("🎯");
        genreIcon.setStyle("-fx-font-size: 18px;");

        genreChoice = new ComboBox<>();
        genreChoice.setPrefWidth(180);
        genreChoice.setPromptText("All Genres");
        genreChoice.setStyle("-fx-background-radius: 10; -fx-font-size: 14px;");

        Button resetBtn = new Button("↺ Reset");
        resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 15; -fx-font-weight: 600;");
        resetBtn.setOnMouseEntered(e -> resetBtn.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 15; -fx-font-weight: 600;"));
        resetBtn.setOnMouseExited(e -> resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 15; -fx-font-weight: 600;"));
        resetBtn.setOnAction(e -> onReset());

        filterBar.getChildren().addAll(searchIcon, searchField, filterIcon, categoryChoice, platformIcon, platformChoice, genreIcon, genreChoice, resetBtn);
        return filterBar;
    }

    private HBox createBottomBar() {
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(20));
        bottomBar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 1 0 0 0;");

        prevBtn = new Button("← Previous");
        prevBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        prevBtn.setOnMouseEntered(e -> prevBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        prevBtn.setOnMouseExited(e -> prevBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        prevBtn.setOnAction(e -> onPrevPage());

        pageLabel = new Label("Page 1");
        pageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        nextBtn = new Button("Next →");
        nextBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        nextBtn.setOnMouseEntered(e -> nextBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        nextBtn.setOnMouseExited(e -> nextBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        nextBtn.setOnAction(e -> onNextPage());

        bottomBar.getChildren().addAll(prevBtn, pageLabel, nextBtn);
        return bottomBar;
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

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String category = categoryChoice.getValue();
        String platform = platformChoice.getValue();
        String genre = genreChoice.getValue();
        String sortOption = sortChoice.getValue();

        // FILTER PRODUCTS
        filteredProducts = allProducts.stream()
                .filter(p -> (category.equals("All") || p.getCategory().equalsIgnoreCase(category)))
                .filter(p -> (platform == null || platform.equals("All Platforms") ||
                        p.getPlatforms().contains(platform)))
                .filter(p -> (genre == null || genre.equals("All Genres") ||
                        p.getGenres().contains(genre)))
                .filter(p -> p.getName().toLowerCase().contains(keyword) ||
                        p.getCategory().toLowerCase().contains(keyword))
                .collect(java.util.stream.Collectors.toList());

        // SORT PRODUCTS
        // SORT PRODUCTS
        switch (sortOption) {
            case "Price: Low → High" ->
                    filteredProducts.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));

            case "Price: High → Low" ->
                    filteredProducts.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));



            case "Newest → Oldest" ->
                    filteredProducts.sort((a, b) -> Integer.compare(b.getId(), a.getId()));

            case "Oldest → Newest" ->
                    filteredProducts.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        }


        currentPage = 1;
        showPage(currentPage, filteredProducts);
    }


    private void onReset() {
        searchField.clear();
        categoryChoice.setValue("All");
        platformChoice.setValue("All Platforms");
        genreChoice.setValue("All Genres");
        sortChoice.setValue("None");
        applyFilters();
    }

    private void loadProducts() {
        allProducts = productService.getAll();
        filteredProducts = new ArrayList<>(allProducts);
        showPage(currentPage, filteredProducts);
    }

    private void loadPlatforms() {
        allPlatforms = productDao.getAllPlatforms();
        platformChoice.getItems().add("All Platforms");
        for (Platform platform : allPlatforms) {
            platformChoice.getItems().add(platform.getName());
        }
        platformChoice.setValue("All Platforms");
    }

    private void loadGenres() {
        allGenres = productDao.getAllGenres();
        genreChoice.getItems().add("All Genres");
        for (Genre genre : allGenres) {
            genreChoice.getItems().add(genre.getName());
        }
        genreChoice.setValue("All Genres");
    }

    private void showPage(int page, List<Product> list) {
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, list.size());

        productGrid.getChildren().clear();
        for (int i = start; i < end; i++) {
            Product p = list.get(i);
            VBox card = createProductCard(p);
            productGrid.getChildren().add(card);
        }

        int totalPages = Math.max(1, (int) Math.ceil(list.size() / (double) ITEMS_PER_PAGE));
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevBtn.setDisable(currentPage == 1);
        nextBtn.setDisable(end >= list.size());
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(240);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4); " +
                "-fx-cursor: hand;");

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-border-color: #667eea; -fx-border-radius: 16; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 20, 0, 0, 8); " +
                    "-fx-cursor: hand; -fx-translate-y: -5;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4); " +
                    "-fx-cursor: hand;");
        });

        // Click to open details
        card.setOnMouseClicked(e -> openProductDetails(p));

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 180);
        imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12;");

        ImageView imageView = new ImageView();
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            imageView.setImage(new Image("file:" + p.getImagePath(), 160, 160, true, true));
        }
        imageView.setFitWidth(160);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageContainer.getChildren().add(imageView);

        // Discount badge
        if (p.getDiscount() > 0) {
            Label discountBadge = new Label("-" + (int) p.getDiscount() + "%");
            discountBadge.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                    "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12;");
            StackPane.setAlignment(discountBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(discountBadge, new Insets(10));
            imageContainer.getChildren().add(discountBadge);
        }

        // Product name
        Label nameLabel = new Label(p.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(210);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");

        // Category badge
        Label categoryLabel = new Label(p.getCategory());
        categoryLabel.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057; " +
                "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;");

        // Platform tags
        FlowPane platformTags = new FlowPane();
        platformTags.setHgap(4);
        platformTags.setVgap(4);
        platformTags.setAlignment(Pos.CENTER);
        platformTags.setMaxWidth(210);

        if (p.getPlatforms() != null && !p.getPlatforms().isEmpty()) {
            for (String platform : p.getPlatforms()) {
                Label platformLabel = new Label(getplatformIcon(platform));
                platformLabel.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 600;");
                platformLabel.setTooltip(new Tooltip(platform));
                platformTags.getChildren().add(platformLabel);
            }
        }

        // Genre tags
        FlowPane genreTags = new FlowPane();
        genreTags.setHgap(4);
        genreTags.setVgap(4);
        genreTags.setAlignment(Pos.CENTER);
        genreTags.setMaxWidth(210);

        if (p.getGenres() != null && !p.getGenres().isEmpty()) {
            int genreCount = 0;
            for (String genre : p.getGenres()) {
                if (genreCount >= 3) break; // Show max 3 genres
                Label genreLabel = new Label(genre);
                genreLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 600;");
                genreLabel.setTooltip(new Tooltip(genre));
                genreTags.getChildren().add(genreLabel);
                genreCount++;
            }
            if (p.getGenres().size() > 3) {
                Label moreLabel = new Label("+" + (p.getGenres().size() - 3));
                moreLabel.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                        "-fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 600;");
                moreLabel.setTooltip(new Tooltip("More genres: " + String.join(", ", p.getGenres().subList(3, p.getGenres().size()))));
                genreTags.getChildren().add(moreLabel);
            }
        }

        // Price section
        VBox priceBox = new VBox(3);
        priceBox.setAlignment(Pos.CENTER);

        double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
        Label priceLabel = new Label("$" + String.format("%.2f", finalPrice));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #667eea; -fx-font-weight: bold;");

        if (p.getDiscount() > 0) {
            Label oldPrice = new Label("$" + String.format("%.2f", p.getPrice()));
            oldPrice.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px; -fx-strikethrough: true;");
            priceBox.getChildren().addAll(priceLabel, oldPrice);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        // Rating and stock
        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER);

        Label ratingLabel = new Label("⭐ " + String.format("%.1f", new ReviewService().getAverageRating(p.getId())));
        ratingLabel.setStyle("-fx-text-fill: #f5b301; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label stockLabel = new Label("📦 " + p.getStock());
        stockLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");

        infoBox.getChildren().addAll(ratingLabel, new Separator(javafx.geometry.Orientation.VERTICAL), stockLabel);

        // Click to view button
        Label clickHint = new Label("👆 Click to view details");
        clickHint.setStyle("-fx-text-fill: #667eea; -fx-font-size: 12px; -fx-font-style: italic;");

        card.getChildren().addAll(imageContainer, nameLabel, categoryLabel, platformTags, genreTags, priceBox, infoBox, clickHint);

        return card;
    }

    private String getplatformIcon(String platformName) {
        // Return abbreviated platform names or icons
        return switch (platformName) {
            case "PlayStation 5" -> "PS5";
            case "PlayStation 4" -> "PS4";
            case "Xbox Series X/S" -> "XBS";
            case "Xbox One" -> "XBO";
            case "Nintendo Switch" -> "NSW";
            case "PC (Windows)" -> "WIN";
            case "PC (Steam)" -> "STM";
            case "PC (Epic Games)" -> "EGS";
            case "Steam Deck" -> "SDK";
            case "Meta Quest" -> "VR";
            default -> platformName.substring(0, Math.min(3, platformName.length())).toUpperCase();
        };
    }

    private void openProductDetails(Product p) {
        Stage detailsStage = new Stage();
        detailsStage.initModality(Modality.APPLICATION_MODAL);
        detailsStage.setTitle(p.getName());

        BorderPane root = new BorderPane();
        root.setPrefSize(700, 600);
        root.setStyle("-fx-background-color: white;");

        // Product details section
        VBox detailsBox = createProductDetailsView(p, detailsStage);

        ScrollPane scrollPane = new ScrollPane(detailsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");

        root.setCenter(scrollPane);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(HelloApplication.class.getResource("view/style.css").toExternalForm());
        } catch (Exception ignored) {}

        detailsStage.setScene(scene);
        detailsStage.showAndWait();

        // Refresh after closing
        loadProducts();
    }

    private VBox createProductDetailsView(Product p, Stage stage) {
        VBox details = new VBox(20);
        details.setPadding(new Insets(30));
        details.setAlignment(Pos.TOP_CENTER);

        // Image section
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(300, 300);
        imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 16;");

        ImageView bigImage = new ImageView();
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            bigImage.setImage(new Image("file:" + p.getImagePath(), 280, 280, true, true));
        }
        bigImage.setFitWidth(280);
        bigImage.setFitHeight(280);
        bigImage.setPreserveRatio(true);
        imageContainer.getChildren().add(bigImage);

        // Product info
        Label nameLabel = new Label(p.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(600);
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");

        Label categoryLabel = new Label("Category: " + p.getCategory());
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        // Description
        if (p.getDescription() != null && !p.getDescription().isEmpty()) {
            Label descLabel = new Label(p.getDescription());
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(600);
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-padding: 10; " +
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
            details.getChildren().add(descLabel);
        }

        // Price box
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER);
        priceBox.setPadding(new Insets(15));
        priceBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12;");

        double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
        Label priceLabel = new Label("$" + String.format("%.2f", finalPrice));
        priceLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #667eea; -fx-font-weight: bold;");

        if (p.getDiscount() > 0) {
            HBox discountBox = new HBox(10);
            discountBox.setAlignment(Pos.CENTER);

            Label oldPrice = new Label("$" + String.format("%.2f", p.getPrice()));
            oldPrice.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 18px; -fx-strikethrough: true;");

            Label discountLabel = new Label("Save " + (int) p.getDiscount() + "%!");
            discountLabel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                    "-fx-padding: 4 10; -fx-background-radius: 8; -fx-font-weight: bold;");

            discountBox.getChildren().addAll(oldPrice, discountLabel);
            priceBox.getChildren().addAll(priceLabel, discountBox);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        // Stock and rating
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);

        VBox stockBox = new VBox(5);
        stockBox.setAlignment(Pos.CENTER);
        Label stockIcon = new Label("📦");
        stockIcon.setStyle("-fx-font-size: 24px;");
        Label stockLabel = new Label(p.getStock() + " in stock");
        stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        stockBox.getChildren().addAll(stockIcon, stockLabel);

        VBox ratingBox = new VBox(5);
        ratingBox.setAlignment(Pos.CENTER);
        Label ratingIcon = new Label("⭐");
        ratingIcon.setStyle("-fx-font-size: 24px;");
        Label ratingLabel = new Label(String.format("%.1f", new ReviewService().getAverageRating(p.getId())) + " rating");
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        ratingBox.getChildren().addAll(ratingIcon, ratingLabel);

        statsBox.getChildren().addAll(stockBox, ratingBox);

        // Quantity selector
        HBox qtyBox = new HBox(10);
        qtyBox.setAlignment(Pos.CENTER);

        Label qtyLabel = new Label("Quantity:");
        qtyLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600;");

        Spinner<Integer> qtySpinner = new Spinner<>();
        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, p.getStock(), 1));
        qtySpinner.setPrefWidth(100);
        qtySpinner.setStyle("-fx-font-size: 14px;");

        qtyBox.getChildren().addAll(qtyLabel, qtySpinner);

        // Action buttons
        VBox buttonBox = new VBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(400);

        Button addToCartBtn = new Button("🛒 Add to Cart");
        addToCartBtn.setPrefWidth(350);
        addToCartBtn.setPrefHeight(50);
        addToCartBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12;");
        addToCartBtn.setOnAction(e -> handleAddToCart(p, qtySpinner.getValue(), stage));

        WishlistDao wishlistDao = new WishlistDao();
        int userId = Session.getUserId();
        boolean inWishlist = wishlistDao.isInWishlist(userId, p.getId());

        Button wishlistBtn = new Button(inWishlist ? "❤️ Remove from Wishlist" : "🤍 Add to Wishlist");
        wishlistBtn.setPrefWidth(350);
        wishlistBtn.setPrefHeight(45);
        wishlistBtn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #495057; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10; " +
                "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;");
        wishlistBtn.setOnAction(e -> {
            if (wishlistDao.isInWishlist(userId, p.getId())) {
                wishlistDao.removeFromWishlist(userId, p.getId());
                wishlistBtn.setText("🤍 Add to Wishlist");
                showStyledAlert("Removed", "Removed from wishlist", Alert.AlertType.INFORMATION);
            } else {
                wishlistDao.addToWishlist(userId, p.getId());
                wishlistBtn.setText("❤️ Remove from Wishlist");
                showStyledAlert("Added", "Added to wishlist!", Alert.AlertType.INFORMATION);
            }
        });

        Button reviewBtn = new Button("✍️ Leave a Review");
        reviewBtn.setPrefWidth(350);
        reviewBtn.setPrefHeight(45);
        reviewBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10;");
        reviewBtn.setOnAction(e -> openReviewPopup(p));

        Button viewReviewsBtn = new Button("👀 View All Reviews");
        viewReviewsBtn.setPrefWidth(350);
        viewReviewsBtn.setPrefHeight(45);
        viewReviewsBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10;");
        viewReviewsBtn.setOnAction(e -> openReviewsView(p));

        buttonBox.getChildren().addAll(addToCartBtn, wishlistBtn, reviewBtn, viewReviewsBtn);

        details.getChildren().addAll(imageContainer, nameLabel, categoryLabel, priceBox,
                statsBox, qtyBox, buttonBox);

        return details;
    }

    private void handleAddToCart(Product p, int qty, Stage stage) {
        if (p.getStock() <= 0) {
            showStyledAlert("Out of Stock", p.getName() + " is currently unavailable.", Alert.AlertType.ERROR);
            return;
        }

        if (qty > p.getStock()) {
            showStyledAlert("Insufficient Stock", "Only " + p.getStock() + " items available.", Alert.AlertType.WARNING);
            return;
        }

        double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
        cartService.addItem(p, qty);
        ShoppingCart.addItem(p.getId(), finalPrice, qty, p.getStock());
        productDao.decreaseStock(p.getId(), qty);

        showStyledAlert("Success", qty + " × " + p.getName() + " added to cart!", Alert.AlertType.INFORMATION);
        stage.close();
    }

    private void openReviewPopup(Product p) {
        try {
            ReviewPopupController controller = new ReviewPopupController();
            Parent root = controller.createView();
            controller.setProduct(p.getId(), p.getName());

            Stage stage = new Stage();
            stage.setTitle("Review " + p.getName());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            showStyledAlert("Error", "Failed to open review popup", Alert.AlertType.ERROR);
        }
    }

    private void openReviewsView(Product p) {
        try {
            ProductReviewsController controller = new ProductReviewsController();
            Parent root = controller.createView();
            controller.setProduct(p.getId(), p.getName());

            Stage stage = new Stage();
            stage.setTitle("Reviews for " + p.getName());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
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

    private void onNextPage() {
        if (currentPage * ITEMS_PER_PAGE < filteredProducts.size()) {
            currentPage++;
            showPage(currentPage, filteredProducts);
        }
    }

    private void onPrevPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage, filteredProducts);
        }
    }

    private void onWishlist() {
        HelloApplication.setRoot(new WishlistController());
    }

    private void onProfile() {
        HelloApplication.setRoot(new ProfileSettingsController());
    }

    private void onViewOrders() {
        HelloApplication.setRoot(new OrderHistoryController());
    }

    private void onLogout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }

    private void onViewCart() {
        HelloApplication.setRoot(new CartController());
    }
}