package com.example.finalproject.controller;

import com.example.finalproject.model.Review;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.ReviewService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ReviewPopupController {
    private Label productLabel;
    private HBox starRating;
    private TextArea commentField;
    private int selectedRating = 5;

    private final ReviewService service = new ReviewService();
    private int productId;

    public Parent createView() {
        AuthGuard.requireLogin();

        VBox root = new VBox();
        root.setSpacing(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root.setPrefWidth(450);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

        // Header with icon
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("⭐");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label("Leave a Review");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Product label
        productLabel = new Label();
        productLabel.setWrapText(true);
        productLabel.setMaxWidth(380);
        productLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-text-alignment: center;");

        // Star rating section
        VBox ratingSection = new VBox(12);
        ratingSection.setAlignment(Pos.CENTER);

        Label ratingLabel = new Label("Your Rating");
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        starRating = createStarRating();

        ratingSection.getChildren().addAll(ratingLabel, starRating);

        // Comment section
        VBox commentSection = new VBox(8);
        commentSection.setAlignment(Pos.TOP_LEFT);

        Label commentLabel = new Label("Your Review");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        commentField = new TextArea();
        commentField.setPromptText("Share your thoughts about this product...");
        commentField.setPrefRowCount(4);
        commentField.setWrapText(true);
        commentField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-padding: 12; -fx-font-size: 14px;");
        commentField.setOnMouseEntered(e -> {
            if (!commentField.isFocused()) {
                commentField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; " +
                        "-fx-border-color: #667eea; -fx-border-radius: 12; -fx-border-width: 1; " +
                        "-fx-padding: 12; -fx-font-size: 14px;");
            }
        });
        commentField.setOnMouseExited(e -> {
            if (!commentField.isFocused()) {
                commentField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; " +
                        "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1; " +
                        "-fx-padding: 12; -fx-font-size: 14px;");
            }
        });
        commentField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                commentField.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-border-color: #667eea; -fx-border-radius: 12; -fx-border-width: 2; " +
                        "-fx-padding: 12; -fx-font-size: 14px;");
            } else {
                commentField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; " +
                        "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1; " +
                        "-fx-padding: 12; -fx-font-size: 14px;");
            }
        });

        commentSection.getChildren().addAll(commentLabel, commentField);

        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(140);
        cancelBtn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #6c757d; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #6c757d; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #6c757d; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;"));
        cancelBtn.setOnAction(e -> ((Stage) productLabel.getScene().getWindow()).close());

        Button submitBtn = new Button("Submit Review");
        submitBtn.setPrefWidth(180);
        submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 3);");
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #5568d3, #6a3f8f); -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.6), 15, 0, 0, 5);"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-weight: 600; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 3);"));
        submitBtn.setOnAction(e -> onSubmit());

        buttonBox.getChildren().addAll(cancelBtn, submitBtn);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(
                header,
                productLabel,
                ratingSection,
                commentSection,
                separator,
                buttonBox
        );

        return root;
    }

    private HBox createStarRating() {
        HBox stars = new HBox(8);
        stars.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Label star = new Label("⭐");
            star.setStyle("-fx-font-size: 32px; -fx-cursor: hand; -fx-opacity: " + (i <= selectedRating ? "1.0" : "0.3") + ";");

            star.setOnMouseEntered(e -> {
                for (int j = 0; j < stars.getChildren().size(); j++) {
                    Label s = (Label) stars.getChildren().get(j);
                    s.setStyle("-fx-font-size: 32px; -fx-cursor: hand; -fx-opacity: " + (j < rating ? "1.0" : "0.3") + ";");
                }
            });

            star.setOnMouseExited(e -> {
                for (int j = 0; j < stars.getChildren().size(); j++) {
                    Label s = (Label) stars.getChildren().get(j);
                    s.setStyle("-fx-font-size: 32px; -fx-cursor: hand; -fx-opacity: " + (j < selectedRating ? "1.0" : "0.3") + ";");
                }
            });

            star.setOnMouseClicked(e -> {
                selectedRating = rating;
                for (int j = 0; j < stars.getChildren().size(); j++) {
                    Label s = (Label) stars.getChildren().get(j);
                    s.setStyle("-fx-font-size: 32px; -fx-cursor: hand; -fx-opacity: " + (j < selectedRating ? "1.0" : "0.3") + ";");
                }
            });

            stars.getChildren().add(star);
        }

        return stars;
    }

    public void setProduct(int id, String name) {
        this.productId = id;
        productLabel.setText("Reviewing: " + name);
    }

    private void onSubmit() {
        try {
            String comment = commentField.getText().trim();

            if (comment.isEmpty()) {
                showStyledAlert("Incomplete Review", "Please write a comment about your experience.", Alert.AlertType.WARNING);
                return;
            }

            int userId = JwtService.getUserId(Session.getToken());
            Review r = new Review(0, productId, userId, selectedRating, comment, null);
            service.addReview(r);

            showStyledAlert("Success", "✅ Your review has been submitted successfully!", Alert.AlertType.INFORMATION);
            ((Stage) productLabel.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Error", "❌ Failed to submit review: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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