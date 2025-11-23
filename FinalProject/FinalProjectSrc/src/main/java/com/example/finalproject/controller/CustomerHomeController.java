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
import com.example.finalproject.service.LoyaltyService;
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
import java.util.Map;
import java.util.stream.Collectors;

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
    private ComboBox<String> ageRatingChoice;
    private ComboBox<String> priceRangeChoice;
    private List<Platform> allPlatforms;
    private List<Genre> allGenres;
    private List<Product> filteredProducts;
    private ComboBox<String> sortChoice;
    private Label resultsCountLabel;
    private Label activeFiltersLabel;
    private final ReviewService reviewService = new ReviewService();
    private final LoyaltyService loyaltyService = new LoyaltyService();

    // Statistics card labels (to update after data loads)
    private Label statsProductsValue;
    private Label statsPlatformsValue;
    private Label statsGenresValue;
    private Label statsCategoriesValue;

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(1200, 750);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar
        VBox topSection = new VBox();
        topSection.getChildren().addAll(createTopBar(), createFilterBar(), createStatisticsCards());

        root.setTop(topSection);

        // Center - Featured Products + Product Grid
        VBox centerContent = new VBox(0);
        centerContent.setStyle("-fx-background-color: transparent;");

        // Featured products section
        VBox featuredSection = createFeaturedProductsSection();

        // Product Grid
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
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        centerContent.getChildren().addAll(featuredSection, scrollPane);
        root.setCenter(centerContent);

        // Bottom - Pagination
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);

        // Initialize
        loadProducts();
        loadPlatforms();
        loadGenres();
        updateStatistics(); // Update statistics after data is loaded
        categoryChoice.getItems().addAll("All Categories", "Console", "PC", "Accessory", "Game", "Controller");
        categoryChoice.setValue("All Categories");

        // Initialize age rating filter
        ageRatingChoice.getItems().addAll("All Age Ratings", "E (Everyone)", "E10+ (Everyone 10+)", "T (Teen)", "M (Mature 17+)", "AO (Adults Only)");
        ageRatingChoice.setValue("All Age Ratings");

        // Initialize price range filter
        priceRangeChoice.getItems().addAll("All Price Ranges", "$0 - $20", "$20 - $40", "$40 - $60", "$60 - $100", "$100 - $500", "$500+");
        priceRangeChoice.setValue("All Price Ranges");

        // Listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        platformChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        genreChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        ageRatingChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        priceRangeChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        return root;
    }
    /**
     * Create featured products section with horizontal scrolling - HIDDEN BY DEFAULT
     */
    private VBox createFeaturedProductsSection() {
        VBox featuredContainer = new VBox(15);
        featuredContainer.setPadding(new Insets(10, 30, 10, 30));
        featuredContainer.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        // Toggle button to show/hide featured section
        Button toggleBtn = new Button("▼ Show Featured Products");
        toggleBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 15; -fx-font-size: 12px; -fx-font-weight: 600; -fx-cursor: hand;");

        // Content container (initially hidden)
        VBox contentContainer = new VBox(15);
        contentContainer.setVisible(false);
        contentContainer.setManaged(false);

        // Get featured products
        List<Product> bestDeals = allProducts != null ? allProducts.stream()
                .filter(p -> p.getDiscount() > 0)
                .sorted((a, b) -> Double.compare(b.getDiscount(), a.getDiscount()))
                .limit(5)
                .collect(Collectors.toList()) : new ArrayList<>();

        List<Product> topRated = allProducts != null ? allProducts.stream()
                .sorted((a, b) -> Double.compare(
                        reviewService.getAverageRating(b.getId()),
                        reviewService.getAverageRating(a.getId())))
                .limit(5)
                .collect(Collectors.toList()) : new ArrayList<>();

        List<Product> newArrivals = allProducts != null ? allProducts.stream()
                .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                .limit(5)
                .collect(Collectors.toList()) : new ArrayList<>();

        // Only show toggle if we have products
        if (allProducts == null || allProducts.isEmpty()) {
            featuredContainer.setVisible(false);
            featuredContainer.setManaged(false);
            return featuredContainer;
        }

        // Best Deals Section
        if (!bestDeals.isEmpty()) {
            VBox dealsSection = createFeaturedCategory("🔥 Hot Deals", bestDeals, "#dc3545");
            contentContainer.getChildren().add(dealsSection);
        }

        // Top Rated Section
        if (!topRated.isEmpty()) {
            VBox topRatedSection = createFeaturedCategory("⭐ Top Rated", topRated, "#ffc107");
            contentContainer.getChildren().add(topRatedSection);
        }

        // New Arrivals Section
        if (!newArrivals.isEmpty()) {
            VBox newSection = createFeaturedCategory("🆕 New Arrivals", newArrivals, "#28a745");
            contentContainer.getChildren().add(newSection);
        }

        // Toggle button action
        toggleBtn.setOnAction(e -> {
            boolean isVisible = contentContainer.isVisible();
            contentContainer.setVisible(!isVisible);
            contentContainer.setManaged(!isVisible);
            toggleBtn.setText(isVisible ? "▼ Show Featured Products" : "▲ Hide Featured Products");
        });

        // Only show toggle button if we have featured products
        if (contentContainer.getChildren().isEmpty()) {
            featuredContainer.setVisible(false);
            featuredContainer.setManaged(false);
        } else {
            featuredContainer.getChildren().addAll(toggleBtn, contentContainer);
        }

        return featuredContainer;
    }

    /**
     * Create a featured category section with horizontal scroll
     */
    private VBox createFeaturedCategory(String title, List<Product> products, String accentColor) {
        VBox section = new VBox(10);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        // Horizontal scroll pane for products - more compact
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(170);

        HBox productRow = new HBox(15);
        productRow.setPadding(new Insets(10, 0, 10, 0));

        for (Product p : products) {
            VBox miniCard = createMiniProductCard(p, accentColor);
            productRow.getChildren().add(miniCard);
        }

        scrollPane.setContent(productRow);

        section.getChildren().addAll(titleLabel, scrollPane);
        return section;
    }

    /**
     * Create a compact product card for featured section
     */
    private VBox createMiniProductCard(Product p, String accentColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(180);
        card.setMaxWidth(180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2); " +
                "-fx-cursor: hand;");

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: " + accentColor + "; -fx-border-radius: 12; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, " + accentColor + "40, 15, 0, 0, 5); " +
                    "-fx-cursor: hand; -fx-translate-y: -3;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2); " +
                    "-fx-cursor: hand;");
        });

        card.setOnMouseClicked(e -> openProductDetails(p));

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(120, 120);
        imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        ImageView imageView = new ImageView();
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            imageView.setImage(new Image("file:" + p.getImagePath(), 100, 100, true, true));
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageContainer.getChildren().add(imageView);

        // Discount badge
        if (p.getDiscount() > 0) {
            Label discountBadge = new Label("-" + (int) p.getDiscount() + "%");
            discountBadge.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                    "-fx-padding: 3 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 10px;");
            StackPane.setAlignment(discountBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(discountBadge, new Insets(5));
            imageContainer.getChildren().add(discountBadge);
        }

        // Product name
        Label nameLabel = new Label(p.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(160);
        nameLabel.setMaxHeight(35);
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");

        // Price
        double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
        Label priceLabel = new Label("$" + String.format("%.2f", finalPrice));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + accentColor + "; -fx-font-weight: bold;");

        // Rating
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", reviewService.getAverageRating(p.getId())));
        ratingLabel.setStyle("-fx-text-fill: #f5b301; -fx-font-weight: 600; -fx-font-size: 11px;");

        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, ratingLabel);

        return card;
    }


    /**
     * Create statistics cards showing key metrics - COMPACT VERSION
     */
    private HBox createStatisticsCards() {
        HBox statsContainer = new HBox(15);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(10, 30, 10, 30));
        statsContainer.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        // Compact card styling
        String cardStyle = "-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                "-fx-padding: 8 15; -fx-border-color: #e1e4e8; -fx-border-width: 1; -fx-border-radius: 8;";
        String iconStyle = "-fx-font-size: 18px;";
        String labelStyle = "-fx-font-size: 11px; -fx-text-fill: #6c757d; -fx-font-weight: 600;";
        String valueStyle = "-fx-font-size: 18px; -fx-font-weight: bold;";

        // Calculate statistics
        int totalProducts = allProducts != null ? allProducts.size() : 0;
        int totalPlatforms = allPlatforms != null ? allPlatforms.size() : 0;
        int totalGenres = allGenres != null ? allGenres.size() : 0;

        // Count unique categories
        long totalCategories = 0;
        if (allProducts != null) {
            totalCategories = allProducts.stream()
                    .map(p -> p.getCategory())
                    .distinct()
                    .count();
        }

        // Create interactive compact cards with tooltips
        statsProductsValue = new Label(String.valueOf(totalProducts));
        HBox card1 = createCompactStatCard("📦", "Products", statsProductsValue,
                cardStyle, iconStyle, labelStyle, valueStyle, "#667eea");
        card1.setOnMouseClicked(e -> {
            onReset(); // Show all products
        });
        Tooltip.install(card1, new Tooltip("Click to view all products"));

        statsPlatformsValue = new Label(String.valueOf(totalPlatforms));
        HBox card2 = createCompactStatCard("🎮", "Platforms", statsPlatformsValue,
                cardStyle, iconStyle, labelStyle, valueStyle, "#28a745");
        card2.setOnMouseClicked(e -> {
            showPlatformBreakdown();
        });
        Tooltip.install(card2, new Tooltip("Click to see platform breakdown"));

        statsGenresValue = new Label(String.valueOf(totalGenres));
        HBox card3 = createCompactStatCard("🎯", "Genres", statsGenresValue,
                cardStyle, iconStyle, labelStyle, valueStyle, "#17a2b8");
        card3.setOnMouseClicked(e -> {
            showGenreBreakdown();
        });
        Tooltip.install(card3, new Tooltip("Click to see genre breakdown"));

        statsCategoriesValue = new Label(String.valueOf(totalCategories));
        HBox card4 = createCompactStatCard("🏷️", "Categories", statsCategoriesValue,
                cardStyle, iconStyle, labelStyle, valueStyle, "#ffc107");
        card4.setOnMouseClicked(e -> {
            showCategoryBreakdown();
        });
        Tooltip.install(card4, new Tooltip("Click to see category breakdown"));

        statsContainer.getChildren().addAll(card1, card2, card3, card4);

        return statsContainer;
    }

    /**
     * Helper method to create compact horizontal stat card
     */
    private HBox createCompactStatCard(String icon, String label, Label valueLabel,
                                        String cardStyle, String iconStyle,
                                        String labelStyle, String valueStyle,
                                        String accentColor) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(40);
        card.setStyle(cardStyle + " -fx-cursor: hand;");

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(iconStyle);

        // Info container
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Value (passed in as parameter to allow updates)
        valueLabel.setStyle(valueStyle + " -fx-text-fill: " + accentColor + ";");

        // Label
        Label textLabel = new Label(label);
        textLabel.setStyle(labelStyle);

        infoBox.getChildren().addAll(valueLabel, textLabel);
        card.getChildren().addAll(iconLabel, infoBox);

        // Hover effect
        String originalStyle = cardStyle + " -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: " + accentColor + "15; -fx-background-radius: 8; " +
                "-fx-padding: 8 15; -fx-border-color: " + accentColor + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;";

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(originalStyle));

        return card;
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

        // Loyalty points badge
        int userPoints = loyaltyService.getPoints(Session.getUserId());
        String pointsTier = loyaltyService.getPointsTier(userPoints);
        HBox pointsBadge = new HBox(8);
        pointsBadge.setAlignment(Pos.CENTER);
        pointsBadge.setPadding(new Insets(8, 15, 8, 15));
        pointsBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-width: 1; -fx-border-radius: 20;");

        Label pointsIcon = new Label("💎");
        pointsIcon.setStyle("-fx-font-size: 18px;");

        Label pointsLabel = new Label(String.format("%,d Points", userPoints));
        pointsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label tierLabel = new Label(pointsTier);
        tierLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");

        VBox pointsInfo = new VBox(2);
        pointsInfo.setAlignment(Pos.CENTER_LEFT);
        pointsInfo.getChildren().addAll(pointsLabel, tierLabel);

        pointsBadge.getChildren().addAll(pointsIcon, pointsInfo);

        // Hover effect
        pointsBadge.setOnMouseEntered(e -> {
            pointsBadge.setStyle("-fx-background-color: rgba(255,255,255,0.3); " +
                    "-fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.5); " +
                    "-fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;");
        });
        pointsBadge.setOnMouseExited(e -> {
            pointsBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                    "-fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 20;");
        });

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

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, pointsBadge, cartBtn, wishlistBtn, ordersBtn, profileBtn, logoutBtn);
        return topBar;
    }

    private VBox createFilterBar() {
        VBox filterContainer = new VBox(0);
        filterContainer.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        // Row 1: Search and main filters
        HBox filterRow1 = new HBox(15);
        filterRow1.setAlignment(Pos.CENTER_LEFT);
        filterRow1.setPadding(new Insets(15, 30, 10, 30));

        // Search field with icon inside
        StackPane searchContainer = new StackPane();
        searchContainer.setPrefWidth(350);

        searchField = new TextField();
        searchField.setPromptText("Search products by name or category...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-padding: 10 15 10 40; -fx-font-size: 14px;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");
        searchIcon.setMouseTransparent(true);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));

        searchContainer.getChildren().addAll(searchField, searchIcon);

        // Main filters with icons
        HBox categoryBox = createFilterWithIcon("🏷️", createStyledComboBox(160, "Category"));
        categoryChoice = (ComboBox<String>) ((HBox) categoryBox.getChildren().get(1)).getChildren().get(0);

        HBox platformBox = createFilterWithIcon("🎮", createStyledComboBox(180, "Platform"));
        platformChoice = (ComboBox<String>) ((HBox) platformBox.getChildren().get(1)).getChildren().get(0);

        HBox genreBox = createFilterWithIcon("🎯", createStyledComboBox(160, "Genre"));
        genreChoice = (ComboBox<String>) ((HBox) genreBox.getChildren().get(1)).getChildren().get(0);

        filterRow1.getChildren().addAll(searchContainer, categoryBox, platformBox, genreBox);

        // Row 2: Additional filters and actions
        HBox filterRow2 = new HBox(15);
        filterRow2.setAlignment(Pos.CENTER_LEFT);
        filterRow2.setPadding(new Insets(0, 30, 15, 30));

        HBox ratingBox = createFilterWithIcon("🔞", createStyledComboBox(140, "Age Rating"));
        ageRatingChoice = (ComboBox<String>) ((HBox) ratingBox.getChildren().get(1)).getChildren().get(0);

        HBox priceBox = createFilterWithIcon("💰", createStyledComboBox(130, "Price Range"));
        priceRangeChoice = (ComboBox<String>) ((HBox) priceBox.getChildren().get(1)).getChildren().get(0);

        // Sort By dropdown
        Label sortLabel = new Label("Sort:");
        sortLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        sortChoice = new ComboBox<>();
        sortChoice.setPrefWidth(160);
        sortChoice.setPrefHeight(35);
        sortChoice.getItems().addAll(
                "None",
                "Price: Low → High",
                "Price: High → Low",
                "Rating: High → Low",
                "Newest → Oldest",
                "Oldest → Newest"
        );
        sortChoice.setValue("None");
        sortChoice.setStyle("-fx-background-radius: 8; -fx-font-size: 13px; " +
                "-fx-background-color: #f8f9fa; -fx-border-color: #e1e4e8; -fx-border-radius: 8;");
        sortChoice.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> applyFilters());

        HBox sortBox = new HBox(8);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.getChildren().addAll(sortLabel, sortChoice);

        // Reset button with better styling
        Button resetBtn = new Button("↺ Clear All");
        resetBtn.setPrefHeight(35);
        resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: 600; -fx-font-size: 13px;");
        resetBtn.setOnMouseEntered(e -> resetBtn.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: 600; -fx-font-size: 13px; -fx-cursor: hand;"));
        resetBtn.setOnMouseExited(e -> resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: 600; -fx-font-size: 13px;"));
        resetBtn.setOnAction(e -> onReset());

        // Spacer to push results count and active filters to the right
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Active filters indicator
        activeFiltersLabel = new Label("🎯 0 Filters Active");
        activeFiltersLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #28a745; " +
                "-fx-padding: 6 10; -fx-background-color: #e8f5e9; -fx-background-radius: 8;");
        activeFiltersLabel.setVisible(false); // Hidden by default

        // Results count label
        resultsCountLabel = new Label("Showing 0 of 0 products");
        resultsCountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #667eea; " +
                "-fx-padding: 8 12; -fx-background-color: #f0f3ff; -fx-background-radius: 8;");

        filterRow2.getChildren().addAll(ratingBox, priceBox, sortBox, resetBtn, spacer, activeFiltersLabel, resultsCountLabel);

        filterContainer.getChildren().addAll(filterRow1, filterRow2);
        return filterContainer;
    }

    /**
     * Helper method to create a filter with icon
     */
    private HBox createFilterWithIcon(String icon, ComboBox<String> comboBox) {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        HBox comboContainer = new HBox();
        comboContainer.getChildren().add(comboBox);

        container.getChildren().addAll(iconLabel, comboContainer);
        return container;
    }

    /**
     * Helper method to create a styled combo box
     */
    private ComboBox<String> createStyledComboBox(int width, String type) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefWidth(width);
        combo.setPrefHeight(35);
        combo.setPromptText("All " + type + "s");
        combo.setStyle("-fx-background-radius: 8; -fx-font-size: 13px; " +
                "-fx-background-color: #f8f9fa; -fx-border-color: #e1e4e8; -fx-border-radius: 8;");
        return combo;
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
        String ageRating = ageRatingChoice.getValue();
        String priceRange = priceRangeChoice.getValue();
        String sortOption = sortChoice.getValue();

        // Extract rating code from display string (e.g., "M (Mature 17+)" -> "M")
        String ratingCode = null;
        if (ageRating != null && !ageRating.equals("All Age Ratings")) {
            ratingCode = ageRating.split(" ")[0]; // Get first part (E, E10+, T, M, AO)
        }

        String finalRatingCode = ratingCode;

        // Parse price range
        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;
        if (priceRange != null && !priceRange.equals("All Price Ranges")) {
            if (priceRange.equals("$500+")) {
                minPrice = 500;
                maxPrice = Double.MAX_VALUE;
            } else {
                String[] parts = priceRange.replace("$", "").split(" - ");
                minPrice = Double.parseDouble(parts[0]);
                maxPrice = Double.parseDouble(parts[1]);
            }
        }

        double finalMinPrice = minPrice;
        double finalMaxPrice = maxPrice;

        // FILTER PRODUCTS
        filteredProducts = allProducts.stream()
                .filter(p -> (category.equals("All Categories") || p.getCategory().equalsIgnoreCase(category)))
                .filter(p -> (platform == null || platform.equals("All Platforms") ||
                        p.getPlatforms().contains(platform)))
                .filter(p -> (genre == null || genre.equals("All Genres") ||
                        p.getGenres().contains(genre)))
                .filter(p -> (finalRatingCode == null ||
                        (p.getAgeRating() != null && p.getAgeRating().equals(finalRatingCode))))
                .filter(p -> p.getEffectivePrice() >= finalMinPrice && p.getEffectivePrice() <= finalMaxPrice)
                .filter(p -> p.getName().toLowerCase().contains(keyword) ||
                        p.getCategory().toLowerCase().contains(keyword))
                .collect(java.util.stream.Collectors.toList());

        // SORT PRODUCTS
        switch (sortOption) {
            case "Price: Low → High" ->
                    filteredProducts.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));

            case "Price: High → Low" ->
                    filteredProducts.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));

            case "Rating: High → Low" ->
                    filteredProducts.sort((a, b) -> Double.compare(
                            reviewService.getAverageRating(b.getId()),
                            reviewService.getAverageRating(a.getId())
                    ));

            case "Newest → Oldest" ->
                    filteredProducts.sort((a, b) -> Integer.compare(b.getId(), a.getId()));

            case "Oldest → Newest" ->
                    filteredProducts.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        }

        // Update results count
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(String.format("Showing %d of %d products",
                    filteredProducts.size(), allProducts.size()));
        }

        // Update active filters indicator
        updateActiveFiltersIndicator();

        currentPage = 1;
        showPage(currentPage, filteredProducts);
    }

    /**
     * Count and display active filters
     */
    private void updateActiveFiltersIndicator() {
        if (activeFiltersLabel == null) return;

        int activeFilters = 0;

        // Count each active filter
        if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
            activeFilters++;
        }
        if (categoryChoice.getValue() != null && !categoryChoice.getValue().equals("All Categories")) {
            activeFilters++;
        }
        if (platformChoice.getValue() != null && !platformChoice.getValue().equals("All Platforms")) {
            activeFilters++;
        }
        if (genreChoice.getValue() != null && !genreChoice.getValue().equals("All Genres")) {
            activeFilters++;
        }
        if (ageRatingChoice.getValue() != null && !ageRatingChoice.getValue().equals("All Age Ratings")) {
            activeFilters++;
        }
        if (priceRangeChoice.getValue() != null && !priceRangeChoice.getValue().equals("All Price Ranges")) {
            activeFilters++;
        }
        if (sortChoice.getValue() != null && !sortChoice.getValue().equals("None")) {
            activeFilters++;
        }

        // Update label visibility and text
        if (activeFilters > 0) {
            activeFiltersLabel.setText(String.format("🎯 %d Filter%s Active",
                    activeFilters, activeFilters == 1 ? "" : "s"));
            activeFiltersLabel.setVisible(true);
        } else {
            activeFiltersLabel.setVisible(false);
        }
    }


    private void onReset() {
        searchField.clear();
        categoryChoice.setValue("All Categories");
        platformChoice.setValue("All Platforms");
        genreChoice.setValue("All Genres");
        ageRatingChoice.setValue("All Age Ratings");
        priceRangeChoice.setValue("All Price Ranges");
        sortChoice.setValue("None");
        applyFilters();
    }

    /**
     * Update statistics cards with current data
     */
    private void updateStatistics() {
        if (statsProductsValue != null) {
            int totalProducts = allProducts != null ? allProducts.size() : 0;
            statsProductsValue.setText(String.valueOf(totalProducts));
        }

        if (statsPlatformsValue != null) {
            int totalPlatforms = allPlatforms != null ? allPlatforms.size() : 0;
            statsPlatformsValue.setText(String.valueOf(totalPlatforms));
        }

        if (statsGenresValue != null) {
            int totalGenres = allGenres != null ? allGenres.size() : 0;
            statsGenresValue.setText(String.valueOf(totalGenres));
        }

        if (statsCategoriesValue != null) {
            long totalCategories = 0;
            if (allProducts != null) {
                totalCategories = allProducts.stream()
                        .map(p -> p.getCategory())
                        .distinct()
                        .count();
            }
            statsCategoriesValue.setText(String.valueOf(totalCategories));
        }
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
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(16));
        card.setPrefWidth(260);
        card.setPrefHeight(520);
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

        // Product name with better spacing
        Label nameLabel = new Label(p.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(230);
        nameLabel.setMinHeight(40);
        nameLabel.setMaxHeight(40);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50; " +
                "-fx-text-alignment: center; -fx-alignment: center;");

        // Category and Age Rating badges in separate rows for clarity
        VBox badgeContainer = new VBox(6);
        badgeContainer.setAlignment(Pos.CENTER);

        HBox categoryRow = new HBox(6);
        categoryRow.setAlignment(Pos.CENTER);
        Label categoryLabel = new Label(p.getCategory());
        categoryLabel.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 600;");
        categoryRow.getChildren().add(categoryLabel);

        // Age rating badge (only for games with ratings)
        if (p.getAgeRating() != null && !p.getAgeRating().isEmpty()) {
            Label ratingLabel = new Label(p.getAgeRating());
            String ratingColor = getRatingColor(p.getAgeRating());
            ratingLabel.setStyle("-fx-background-color: " + ratingColor + "; -fx-text-fill: white; " +
                    "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
            ratingLabel.setTooltip(new Tooltip(getRatingDescription(p.getAgeRating())));
            categoryRow.getChildren().add(ratingLabel);
        }

        badgeContainer.getChildren().add(categoryRow);

        // Platform and Genre tags combined
        FlowPane allTags = new FlowPane();
        allTags.setHgap(4);
        allTags.setVgap(4);
        allTags.setAlignment(Pos.CENTER);
        allTags.setMaxWidth(230);

        // Add platform tags (max 2)
        if (p.getPlatforms() != null && !p.getPlatforms().isEmpty()) {
            int platformCount = 0;
            for (String platform : p.getPlatforms()) {
                if (platformCount >= 2) break;
                Label platformLabel = new Label(getplatformIcon(platform));
                platformLabel.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 600;");
                platformLabel.setTooltip(new Tooltip(platform));
                allTags.getChildren().add(platformLabel);
                platformCount++;
            }
            if (p.getPlatforms().size() > 2) {
                Label moreLabel = new Label("+" + (p.getPlatforms().size() - 2) + " more");
                moreLabel.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                        "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-size: 9px; -fx-font-weight: 600;");
                allTags.getChildren().add(moreLabel);
            }
        }

        // Add genre tags (max 2)
        if (p.getGenres() != null && !p.getGenres().isEmpty()) {
            int genreCount = 0;
            for (String genre : p.getGenres()) {
                if (genreCount >= 2) break;
                Label genreLabel = new Label(genre);
                genreLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 600;");
                genreLabel.setTooltip(new Tooltip(genre));
                allTags.getChildren().add(genreLabel);
                genreCount++;
            }
            if (p.getGenres().size() > 2) {
                Label moreLabel = new Label("+" + (p.getGenres().size() - 2) + " more");
                moreLabel.setStyle("-fx-background-color: #218838; -fx-text-fill: white; " +
                        "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-size: 9px; -fx-font-weight: 600;");
                allTags.getChildren().add(moreLabel);
            }
        }

        badgeContainer.getChildren().add(allTags);

        // Separator line
        Separator separator1 = new Separator();
        separator1.setMaxWidth(220);
        separator1.setStyle("-fx-background-color: #e1e4e8;");

        // Price section - more prominent
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER);
        priceBox.setPadding(new Insets(8, 0, 8, 0));

        double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
        Label priceLabel = new Label("$" + String.format("%.2f", finalPrice));
        priceLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #667eea; -fx-font-weight: bold;");

        if (p.getDiscount() > 0) {
            HBox oldPriceBox = new HBox(8);
            oldPriceBox.setAlignment(Pos.CENTER);
            Label oldPrice = new Label("$" + String.format("%.2f", p.getPrice()));
            oldPrice.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px; -fx-strikethrough: true;");
            Label saveLabel = new Label("Save " + (int)p.getDiscount() + "%");
            saveLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11px; -fx-font-weight: bold;");
            oldPriceBox.getChildren().addAll(oldPrice, saveLabel);
            priceBox.getChildren().addAll(priceLabel, oldPriceBox);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        // Rating and stock with better styling
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(5, 0, 5, 0));

        // Rating
        VBox ratingBox = new VBox(2);
        ratingBox.setAlignment(Pos.CENTER);
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", new ReviewService().getAverageRating(p.getId())));
        ratingLabel.setStyle("-fx-text-fill: #f5b301; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label ratingText = new Label("Rating");
        ratingText.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        ratingBox.getChildren().addAll(ratingLabel, ratingText);

        // Stock with status
        VBox stockBox = new VBox(2);
        stockBox.setAlignment(Pos.CENTER);
        Label stockLabel = new Label(p.getStock() > 0 ? "📦 " + p.getStock() : "Out of Stock");
        String stockColor = p.getStock() > 10 ? "#28a745" : (p.getStock() > 0 ? "#ffc107" : "#dc3545");
        stockLabel.setStyle("-fx-text-fill: " + stockColor + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label stockText = new Label("In Stock");
        stockText.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        stockBox.getChildren().addAll(stockLabel, stockText);

        infoBox.getChildren().addAll(ratingBox, new Separator(javafx.geometry.Orientation.VERTICAL), stockBox);

        // Another separator
        Separator separator2 = new Separator();
        separator2.setMaxWidth(220);
        separator2.setStyle("-fx-background-color: #e1e4e8;");

        // Quick action buttons with improved styling
        HBox quickActions = new HBox(8);
        quickActions.setAlignment(Pos.CENTER);
        quickActions.setPrefWidth(230);

        // Quick add to cart button
        Button quickAddBtn = new Button("🛒 Add to Cart");
        quickAddBtn.setPrefWidth(165);
        quickAddBtn.setPrefHeight(38);
        quickAddBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        quickAddBtn.setOnMouseEntered(e -> {
            quickAddBtn.setStyle("-fx-background-color: linear-gradient(to right, #5568d3, #653a8b); " +
                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand;");
        });
        quickAddBtn.setOnMouseExited(e -> {
            quickAddBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand;");
        });
        quickAddBtn.setOnAction(e -> {
            e.consume(); // Prevent card click event
            handleQuickAddToCart(p);
        });

        // Wishlist toggle button
        WishlistDao wishlistDao = new WishlistDao();
        int userId = Session.getUserId();
        boolean inWishlist = wishlistDao.isInWishlist(userId, p.getId());

        Button wishlistToggle = new Button(inWishlist ? "❤️" : "🤍");
        wishlistToggle.setPrefWidth(60);
        wishlistToggle.setPrefHeight(35);
        String baseWishlistStyle = inWishlist
                ? "-fx-background-color: #dc3545; -fx-text-fill: white; "
                : "-fx-background-color: #f8f9fa; -fx-text-fill: #dc3545; -fx-border-color: #dc3545; -fx-border-width: 1; ";
        wishlistToggle.setStyle(baseWishlistStyle +
                "-fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;");
        wishlistToggle.setOnMouseEntered(e -> {
            String hoverStyle = inWishlist
                    ? "-fx-background-color: #c82333; -fx-text-fill: white; "
                    : "-fx-background-color: #dc3545; -fx-text-fill: white; ";
            wishlistToggle.setStyle(hoverStyle +
                    "-fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;");
        });
        wishlistToggle.setOnMouseExited(e -> {
            wishlistToggle.setStyle(baseWishlistStyle +
                    "-fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;");
        });
        wishlistToggle.setOnAction(e -> {
            e.consume(); // Prevent card click event
            handleQuickWishlistToggle(p, wishlistToggle);
        });

        quickActions.getChildren().addAll(quickAddBtn, wishlistToggle);

        // Spacer to push actions to bottom
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Click to view details hint
        Label clickHint = new Label("Click card for full details");
        clickHint.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px; -fx-font-style: italic;");

        card.getChildren().addAll(
                imageContainer,
                nameLabel,
                badgeContainer,
                separator1,
                priceBox,
                infoBox,
                separator2,
                quickActions,
                spacer,
                clickHint
        );

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

    private String getRatingColor(String rating) {
        // Return color based on ESRB rating
        return switch (rating) {
            case "E" -> "#4CAF50"; // Green - Everyone
            case "E10+" -> "#8BC34A"; // Light Green - Everyone 10+
            case "T" -> "#FFC107"; // Amber - Teen
            case "M" -> "#FF5722"; // Red-Orange - Mature
            case "AO" -> "#D32F2F"; // Dark Red - Adults Only
            default -> "#9E9E9E"; // Gray - Unknown
        };
    }

    private String getRatingDescription(String rating) {
        // Return full description for tooltip
        return switch (rating) {
            case "E" -> "Everyone - Content suitable for all ages";
            case "E10+" -> "Everyone 10+ - Content suitable for ages 10 and up";
            case "T" -> "Teen - Content suitable for ages 13 and up";
            case "M" -> "Mature 17+ - Content suitable for ages 17 and up";
            case "AO" -> "Adults Only 18+ - Content suitable only for adults";
            default -> "Rating: " + rating;
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

    /**
     * Quick add to cart from product card (quantity = 1)
     */
    private void handleQuickAddToCart(Product p) {
        if (p.getStock() <= 0) {
            showStyledAlert("Out of Stock", p.getName() + " is currently unavailable.", Alert.AlertType.ERROR);
            return;
        }

        int qty = 1;
        double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
        cartService.addItem(p, qty);
        ShoppingCart.addItem(p.getId(), finalPrice, qty, p.getStock());
        productDao.decreaseStock(p.getId(), qty);

        showStyledAlert("Added to Cart! 🎉", p.getName() + " has been added to your cart.", Alert.AlertType.INFORMATION);

        // Refresh the product display to update stock
        loadProducts();
    }

    /**
     * Quick wishlist toggle from product card
     */
    private void handleQuickWishlistToggle(Product p, Button button) {
        WishlistDao wishlistDao = new WishlistDao();
        int userId = Session.getUserId();

        if (wishlistDao.isInWishlist(userId, p.getId())) {
            wishlistDao.removeFromWishlist(userId, p.getId());
            button.setText("🤍");
            String baseStyle = "-fx-background-color: #f8f9fa; -fx-text-fill: #dc3545; " +
                    "-fx-border-color: #dc3545; -fx-border-width: 1; " +
                    "-fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;";
            button.setStyle(baseStyle);
            showStyledAlert("Removed", "Removed from wishlist", Alert.AlertType.INFORMATION);
        } else {
            wishlistDao.addToWishlist(userId, p.getId());
            button.setText("❤️");
            String baseStyle = "-fx-background-color: #dc3545; -fx-text-fill: white; " +
                    "-fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;";
            button.setStyle(baseStyle);
            showStyledAlert("Added to Wishlist! ❤️", p.getName() + " has been added to your wishlist.", Alert.AlertType.INFORMATION);
        }
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

    /**
     * Show platform breakdown in a popup
     */
    private void showPlatformBreakdown() {
        if (allPlatforms == null || allPlatforms.isEmpty()) {
            showStyledAlert("No Data", "No platforms available", Alert.AlertType.INFORMATION);
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Platform Breakdown");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label title = new Label("🎮 Platform Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox platformList = new VBox(10);
        for (Platform platform : allPlatforms) {
            long count = allProducts.stream()
                    .filter(p -> p.getPlatforms().contains(platform.getName()))
                    .count();

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand;");

            Label nameLabel = new Label(platform.getName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label countLabel = new Label(count + " products");
            countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

            row.getChildren().addAll(nameLabel, spacer, countLabel);

            // Click to filter
            row.setOnMouseClicked(e -> {
                platformChoice.setValue(platform.getName());
                applyFilters();
                stage.close();
            });
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #667eea; -fx-background-radius: 8; -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand;"));

            platformList.getChildren().add(row);
        }

        ScrollPane scrollPane = new ScrollPane(platformList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        content.getChildren().addAll(title, new Separator(), scrollPane);

        Scene scene = new Scene(content, 500, 500);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Show genre breakdown in a popup
     */
    private void showGenreBreakdown() {
        if (allGenres == null || allGenres.isEmpty()) {
            showStyledAlert("No Data", "No genres available", Alert.AlertType.INFORMATION);
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Genre Breakdown");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label title = new Label("🎯 Genre Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox genreList = new VBox(10);
        for (Genre genre : allGenres) {
            long count = allProducts.stream()
                    .filter(p -> p.getGenres().contains(genre.getName()))
                    .count();

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand;");

            Label nameLabel = new Label(genre.getName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label countLabel = new Label(count + " products");
            countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

            row.getChildren().addAll(nameLabel, spacer, countLabel);

            // Click to filter
            row.setOnMouseClicked(e -> {
                genreChoice.setValue(genre.getName());
                applyFilters();
                stage.close();
            });
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #17a2b8; -fx-background-radius: 8; -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand;"));

            genreList.getChildren().add(row);
        }

        ScrollPane scrollPane = new ScrollPane(genreList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        content.getChildren().addAll(title, new Separator(), scrollPane);

        Scene scene = new Scene(content, 500, 500);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Show category breakdown in a popup
     */
    private void showCategoryBreakdown() {
        if (allProducts == null || allProducts.isEmpty()) {
            showStyledAlert("No Data", "No products available", Alert.AlertType.INFORMATION);
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Category Breakdown");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label title = new Label("🏷️ Category Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Group products by category
        Map<String, Long> categoryCount = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        VBox categoryList = new VBox(10);
        categoryCount.forEach((category, count) -> {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand;");

            Label nameLabel = new Label(category);
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label countLabel = new Label(count + " products");
            countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

            row.getChildren().addAll(nameLabel, spacer, countLabel);

            // Click to filter
            row.setOnMouseClicked(e -> {
                categoryChoice.setValue(category);
                applyFilters();
                stage.close();
            });
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #ffc107; -fx-background-radius: 8; -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand;"));

            categoryList.getChildren().add(row);
        });

        ScrollPane scrollPane = new ScrollPane(categoryList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        content.getChildren().addAll(title, new Separator(), scrollPane);

        Scene scene = new Scene(content, 500, 500);
        stage.setScene(scene);
        stage.showAndWait();
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