package com.example.finalproject.controller;

import com.example.finalproject.dao.ReviewDao;
import com.example.finalproject.model.Review;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ProductReviewsController {

    private Label productTitle;
    private VBox reviewsContainer;
    private Label avgRatingLabel;
    private Label totalReviewsLabel;

    private final ReviewDao dao = new ReviewDao();

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setPrefSize(750, 650);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Scrollable reviews
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        reviewsContainer = new VBox(20);
        reviewsContainer.setPadding(new Insets(20));
        reviewsContainer.setAlignment(Pos.TOP_CENTER);

        scrollPane.setContent(reviewsContainer);
        root.setCenter(scrollPane);

        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 20, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-background-radius: 16 16 0 0;");

        // Icon
        Label iconLabel = new Label("⭐");
        iconLabel.setStyle("-fx-font-size: 48px;");

        // Product title
        productTitle = new Label();
        productTitle.setWrapText(true);
        productTitle.setMaxWidth(650);
        productTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-text-alignment: center;");

        // Rating summary box
        HBox ratingBox = new HBox(30);
        ratingBox.setAlignment(Pos.CENTER);
        ratingBox.setPadding(new Insets(15));
        ratingBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 12;");

        VBox avgBox = new VBox(5);
        avgBox.setAlignment(Pos.CENTER);

        avgRatingLabel = new Label("0.0");
        avgRatingLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label avgSubLabel = new Label("Average Rating");
        avgSubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");

        avgBox.getChildren().addAll(avgRatingLabel, avgSubLabel);

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        sep.setPrefHeight(50);

        VBox totalBox = new VBox(5);
        totalBox.setAlignment(Pos.CENTER);

        totalReviewsLabel = new Label("0");
        totalReviewsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label totalSubLabel = new Label("Total Reviews");
        totalSubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");

        totalBox.getChildren().addAll(totalReviewsLabel, totalSubLabel);

        ratingBox.getChildren().addAll(avgBox, sep, totalBox);

        header.getChildren().addAll(iconLabel, productTitle, ratingBox);

        return header;
    }

    public void setProduct(int productId, String productName) {
        productTitle.setText("Reviews for " + productName);

        var reviews = dao.getReviewsByProductWithUser(productId);

        // Calculate stats
        if (reviews.isEmpty()) {
            avgRatingLabel.setText("N/A");
            totalReviewsLabel.setText("0");

            Label noReviews = new Label("📝 No reviews yet\nBe the first to review this product!");
            noReviews.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-text-alignment: center;");
            noReviews.setWrapText(true);
            reviewsContainer.getChildren().add(noReviews);
            return;
        }

        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        avgRatingLabel.setText(String.format("%.1f", avgRating));
        totalReviewsLabel.setText(String.valueOf(reviews.size()));

        // Display reviews
        for (Review review : reviews) {
            reviewsContainer.getChildren().add(createReviewCard(review));
        }
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMaxWidth(680);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1;");

        // Header with user and rating
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        // User avatar
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 32px; -fx-background-color: white; " +
                "-fx-padding: 8; -fx-background-radius: 50%;");

        // User info box
        VBox userBox = new VBox(4);

        Label userName = new Label(review.getUsername());
        userName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLabel = new Label(review.getCreatedAt() != null ? review.getCreatedAt().toString() : "");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        userBox.getChildren().addAll(userName, dateLabel);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Star rating
        HBox starsBox = new HBox(3);
        starsBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < review.getRating() ? "⭐" : "☆");
            star.setStyle("-fx-font-size: 20px;");
            starsBox.getChildren().add(star);
        }

        header.getChildren().addAll(avatarLabel, userBox, spacer, starsBox);

        // Comment
        Label commentLabel = new Label(review.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-padding: 10 0 0 0;");

        card.getChildren().addAll(header, commentLabel);

        return card;
    }
}