package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.ReviewDao;
import com.example.finalproject.model.Review;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class AdminReviewsController {

    private TableView<Review> reviewTable;
    private TableColumn<Review, String> colUser;
    private TableColumn<Review, String> colProduct;
    private TableColumn<Review, Number> colRating;
    private TableColumn<Review, String> colComment;
    private TableColumn<Review, String> colDate;
    private TextField searchField;
    private Label statsLabel;

    private final ReviewDao dao = new ReviewDao();
    private ObservableList<Review> reviewList;

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(900, 650);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar with gradient
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center content
        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);

        // Load data
        initializeData();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setSpacing(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-padding: 20;");

        // Icon and Title
        Label iconLabel = new Label("⭐");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Manage Reviews");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back");
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

        // Main content container
        VBox contentContainer = new VBox(20);
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(25));
        contentContainer.setMaxWidth(850);
        contentContainer.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Header with search
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Label tableTitle = new Label("📝 Review Management");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search bar with icon
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(8, 15, 8, 15));
        searchBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 16px;");

        searchField = new TextField();
        searchField.setPromptText("Search by user, product, or comment...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

        searchBox.getChildren().addAll(searchIcon, searchField);
        headerBox.getChildren().addAll(tableTitle, spacer, searchBox);

        // Table
        reviewTable = new TableView<>();
        reviewTable.setPrefHeight(380);
        reviewTable.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");

        // Columns
        colUser = new TableColumn<>("👤 User");
        colUser.setPrefWidth(150);
        colUser.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        colUser.setStyle("-fx-alignment: CENTER;");

        colProduct = new TableColumn<>("📦 Product");
        colProduct.setPrefWidth(180);
        colProduct.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProductName()));

        colRating = new TableColumn<>("⭐ Rating");
        colRating.setPrefWidth(100);
        colRating.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getRating()));
        colRating.setCellFactory(column -> new TableCell<Review, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int rating = item.intValue();
                    String stars = "⭐".repeat(rating) + "☆".repeat(5 - rating);
                    setText(stars + " (" + rating + ")");

                    String color = rating >= 4 ? "#28a745" : rating >= 3 ? "#ffc107" : "#dc3545";
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        colComment = new TableColumn<>("💬 Comment");
        colComment.setPrefWidth(220);
        colComment.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getComment()));
        colComment.setCellFactory(column -> new TableCell<Review, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String displayText = item.length() > 30 ? item.substring(0, 30) + "..." : item;
                    setText(displayText);
                    setTooltip(new Tooltip(item));
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        colDate = new TableColumn<>("📅 Date");
        colDate.setPrefWidth(150);
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toString() : ""));
        colDate.setStyle("-fx-alignment: CENTER;");

        reviewTable.getColumns().addAll(colUser, colProduct, colRating, colComment, colDate);

        // Action buttons
        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER);
        buttonsRow.setPadding(new Insets(10, 0, 0, 0));

        Button deleteBtn = new Button("🗑️ Delete Selected");
        deleteBtn.setPrefWidth(180);
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,53,69,0.4), 10, 0, 0, 4);"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;"));
        deleteBtn.setOnAction(e -> onDelete());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setPrefWidth(150);
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;");
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 4);"));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;"));
        refreshBtn.setOnAction(e -> initializeData());

        buttonsRow.getChildren().addAll(deleteBtn, refreshBtn);

        contentContainer.getChildren().addAll(headerBox, reviewTable, buttonsRow);
        centerContent.getChildren().addAll(statsCard, contentContainer);

        return centerContent;
    }

    private HBox createStatsCard() {
        HBox statsCard = new HBox(30);
        statsCard.setAlignment(Pos.CENTER);
        statsCard.setPadding(new Insets(20));
        statsCard.setMaxWidth(850);
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        statsLabel = new Label("Loading statistics...");
        statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        statsCard.getChildren().add(statsLabel);

        return statsCard;
    }

    private void updateStatsCard(List<Review> reviews) {
        HBox statsCard = new HBox(30);
        statsCard.setAlignment(Pos.CENTER);

        int totalReviews = reviews.size();
        double avgRating = reviews.isEmpty() ? 0 :
                reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        long fiveStars = reviews.stream().filter(r -> r.getRating() == 5).count();

        VBox totalBox = createStatBox("📊", String.valueOf(totalReviews), "Total Reviews", "#667eea");
        VBox avgBox = createStatBox("⭐", String.format("%.1f", avgRating), "Average Rating", "#ffc107");
        VBox fiveStarBox = createStatBox("🌟", String.valueOf(fiveStars), "5-Star Reviews", "#28a745");

        statsCard.getChildren().addAll(totalBox, avgBox, fiveStarBox);

        // Update the stats card in the UI
        VBox centerContent = (VBox) ((BorderPane) reviewTable.getParent().getParent().getParent()).getCenter();
        HBox oldStatsCard = (HBox) centerContent.getChildren().get(0);
        centerContent.getChildren().set(0, statsCard);
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

    private void initializeData() {
        // Load data
        List<Review> reviews = dao.getAllReviewsForAdmin();
        reviewList = FXCollections.observableArrayList(reviews);

        // Update stats
        updateStatsCard(reviews);

        // Enable search filtering
        FilteredList<Review> filteredList = new FilteredList<>(reviewList, b -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase().trim();
            filteredList.setPredicate(r -> {
                if (filter.isEmpty()) return true;
                return r.getUsername().toLowerCase().contains(filter)
                        || r.getProductName().toLowerCase().contains(filter)
                        || r.getComment().toLowerCase().contains(filter)
                        || String.valueOf(r.getRating()).contains(filter);
            });
        });

        SortedList<Review> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(reviewTable.comparatorProperty());
        reviewTable.setItems(sortedList);
    }

    private void onDelete() {
        Review selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "⚠️ Please select a review to delete.", Alert.AlertType.WARNING);
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this review?\n\n" +
                "User: " + selected.getUsername() + "\n" +
                "Product: " + selected.getProductName() + "\n" +
                "Rating: " + selected.getRating() + " stars");

        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-padding: 20;");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                java.sql.Connection conn = com.example.finalproject.dao.DBConnection.getInstance();
                java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM review WHERE id=?");
                ps.setInt(1, selected.getId());
                ps.executeUpdate();

                reviewList.remove(selected);
                updateStatsCard(reviewList);
                showStyledAlert("Success", "✅ Review deleted successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showStyledAlert("Error", "❌ Error deleting review: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void onBack() {
        HelloApplication.setRoot(new AdminProductsController());
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