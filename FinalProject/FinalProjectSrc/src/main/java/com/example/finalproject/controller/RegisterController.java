package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.UserDao;
import com.example.finalproject.model.User;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.AuthService;
import com.example.finalproject.service.EmailNotificationService;
import com.example.finalproject.util.LoadingOverlay;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class RegisterController {

    private TextField nameField, emailField, addressField;
    private PasswordField passwordField;
    private Label msgLabel;
    private ProgressIndicator progressIndicator;
    private LoadingOverlay loadingOverlay;

    private final AuthService authService = new AuthService();
    private final UserDao userDao = new UserDao();
    private final EmailNotificationService emailNotificationService = new EmailNotificationService();

    public Parent createView() {
        StackPane root = new StackPane();
        root.setPrefSize(900, 700);
        root.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");

        VBox outerBox = new VBox();
        outerBox.setAlignment(Pos.CENTER);
        outerBox.setSpacing(20);

        VBox authCard = new VBox();
        authCard.setAlignment(Pos.CENTER);
        authCard.setSpacing(18);
        authCard.setMaxWidth(450);
        authCard.setPrefWidth(450);
        authCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 30, 0, 0, 10);");
        authCard.setPadding(new Insets(40, 40, 40, 40));

        // Header section
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("🎮");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Join our gaming community today");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        headerBox.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        // Form fields with labels
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);

        // Name field
        VBox nameBox = createFieldBox("👤 Full Name", "John Doe");
        nameField = (TextField) ((VBox) nameBox.getChildren().get(1)).getChildren().get(0);

        // Email field
        VBox emailBox = createFieldBox("📧 Email Address", "john@example.com");
        emailField = (TextField) ((VBox) emailBox.getChildren().get(1)).getChildren().get(0);

        // Password field with strength indicator
        VBox passBox = createPasswordBox();

        // Address field
        VBox addressBox = createFieldBox("🏠 Address", "123 Main Street");
        addressField = (TextField) ((VBox) addressBox.getChildren().get(1)).getChildren().get(0);

        formBox.getChildren().addAll(nameBox, emailBox, passBox, addressBox);

        // Register button
        Button registerBtn = new Button("Create Account");
        registerBtn.setPrefWidth(370);
        registerBtn.setPrefHeight(50);
        registerBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 15, 0, 0, 5);");
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle("-fx-background-color: linear-gradient(to right, #5568d3, #6a3f8f); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.7), 20, 0, 0, 7);"));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 15, 0, 0, 5);"));
        registerBtn.setOnAction(e -> onRegister());

        // Progress indicator (hidden initially)
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.setVisible(false);

        // Message label
        msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(370);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-alignment: center;");

        // Footer with login link
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER);

        Label footerText = new Label("Already have an account?");
        footerText.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        Hyperlink loginLink = new Hyperlink("Sign In");
        loginLink.setStyle("-fx-text-fill: #667eea; -fx-font-size: 14px; -fx-font-weight: bold;");
        loginLink.setOnAction(e -> goLogin());

        footerBox.getChildren().addAll(footerText, loginLink);

        // Separator
        Separator separator = new Separator();
        separator.setMaxWidth(370);
        separator.setPadding(new Insets(10, 0, 10, 0));

        authCard.getChildren().addAll(
                headerBox,
                formBox,
                registerBtn,
                progressIndicator,
                msgLabel,
                separator,
                footerBox
        );

        outerBox.getChildren().add(authCard);
        root.getChildren().add(outerBox);

        // Add loading overlay
        loadingOverlay = new LoadingOverlay();
        root.getChildren().add(loadingOverlay.getOverlay());

        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), authCard);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        return root;
    }

    private VBox createFieldBox(String labelText, String placeholder) {
        VBox fieldBox = new VBox(6);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        VBox inputWrapper = new VBox();
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setPrefHeight(45);
        field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12 15; -fx-font-size: 14px;");

        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                field.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 10, 0, 0, 0);");
            } else {
                field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px;");
            }
        });

        inputWrapper.getChildren().add(field);
        fieldBox.getChildren().addAll(label, inputWrapper);

        return fieldBox;
    }

    private VBox createPasswordBox() {
        VBox fieldBox = new VBox(6);

        Label label = new Label("🔒 Password");
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        VBox inputWrapper = new VBox(5);
        passwordField = new PasswordField();
        passwordField.setPromptText("Min. 8 characters");
        passwordField.setPrefHeight(45);
        passwordField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12 15; -fx-font-size: 14px;");

        passwordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                passwordField.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 10, 0, 0, 0);");
            } else {
                passwordField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px;");
            }
        });

        // Password strength hint
        Label hintLabel = new Label("Must contain: uppercase, lowercase, number & special character");
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        hintLabel.setWrapText(true);

        inputWrapper.getChildren().addAll(passwordField, hintLabel);
        fieldBox.getChildren().addAll(label, inputWrapper);

        return fieldBox;
    }

    public void onRegister() {
        msgLabel.setText("");
        msgLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 13px; -fx-font-weight: 600;");

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String address = addressField.getText().trim();

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty()) {
            showError("⚠️ All fields are required");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("⚠️ Please enter a valid email address");
            return;
        }

        if (password.length() < 8 || !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") || !password.matches(".*\\d.*") ||
                !password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            showError("⚠️ Password doesn't meet requirements");
            return;
        }

        // Show loading
        loadingOverlay.show("Creating your account...");

        try {
            String token = authService.register(name, email, password, address);

            loadingOverlay.updateMessage("✅ Account created!");

            msgLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: 600;");
            msgLabel.setText("✅ Account created successfully!");

            // Send welcome email
            int userId = JwtService.getUserId(token);
            User newUser = userDao.getUserById(userId).orElse(null);
            if (newUser != null) {
                emailNotificationService.sendWelcomeEmail(newUser);
                System.out.println("✅ Welcome email sent to: " + newUser.getEmail());
            }

            // Delay for user to see success message
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                loadingOverlay.hide();
                Session.setToken(token);
                HelloApplication.setRoot(new CustomerHomeController());
            });
            pause.play();

        } catch (Exception e) {
            loadingOverlay.hide();
            showError("❌ " + e.getMessage());
        }
    }

    private void showError(String message) {
        msgLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 13px; -fx-font-weight: 600;");
        msgLabel.setText(message);
    }

    public void goLogin() {
        HelloApplication.setRoot(new LoginController());
    }
}