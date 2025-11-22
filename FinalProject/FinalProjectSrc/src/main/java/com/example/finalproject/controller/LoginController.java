package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.AuthService;
import com.example.finalproject.util.LoadingOverlay;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class LoginController {
    private TextField emailField;
    private PasswordField passwordField;
    private Label msgLabel;
    private ProgressIndicator progressIndicator;
    private LoadingOverlay loadingOverlay;

    private final AuthService authService = new AuthService();

    public Parent createView() {
        StackPane root = new StackPane();
        root.setPrefSize(900, 700);
        root.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");

        VBox outerBox = new VBox();
        outerBox.setAlignment(Pos.CENTER);
        outerBox.setSpacing(20);

        VBox authCard = new VBox();
        authCard.setAlignment(Pos.CENTER);
        authCard.setSpacing(20);
        authCard.setMaxWidth(420);
        authCard.setPrefWidth(420);
        authCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 30, 0, 0, 10);");
        authCard.setPadding(new Insets(45, 40, 45, 40));

        // Header section
        VBox headerBox = new VBox(12);
        headerBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("🎮");
        iconLabel.setStyle("-fx-font-size: 52px;");

        Label titleLabel = new Label("Welcome Back");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Sign in to continue shopping");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        headerBox.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        // Form fields
        VBox formBox = new VBox(18);
        formBox.setAlignment(Pos.CENTER);

        // Email field
        VBox emailBox = createFieldBox("📧 Email Address", "john@example.com");
        emailField = (TextField) ((VBox) emailBox.getChildren().get(1)).getChildren().get(0);

        // Password field
        VBox passBox = createPasswordBox();

        formBox.getChildren().addAll(emailBox, passBox);

        // Forgot password link
        HBox forgotBox = new HBox();
        forgotBox.setAlignment(Pos.CENTER_RIGHT);
        forgotBox.setPadding(new Insets(0, 0, 5, 0));

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #667eea; -fx-font-size: 13px;");
        forgotPasswordLink.setOnAction(e -> onForgotPassword());
        forgotBox.getChildren().add(forgotPasswordLink);

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setPrefWidth(340);
        loginBtn.setPrefHeight(50);
        loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 15, 0, 0, 5);");
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #5568d3, #6a3f8f); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.7), 20, 0, 0, 7);"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 15, 0, 0, 5);"));
        loginBtn.setOnAction(e -> onLogin());

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.setVisible(false);

        // Message label
        msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(340);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");

        // Footer with register link
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER);

        Label footerText = new Label("Don't have an account?");
        footerText.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        Hyperlink registerLink = new Hyperlink("Sign Up");
        registerLink.setStyle("-fx-text-fill: #667eea; -fx-font-size: 14px; -fx-font-weight: bold;");
        registerLink.setOnAction(e -> goRegister());

        footerBox.getChildren().addAll(footerText, registerLink);

        // Separator
        Separator separator = new Separator();
        separator.setMaxWidth(340);
        separator.setPadding(new Insets(15, 0, 5, 0));

        authCard.getChildren().addAll(
                headerBox,
                formBox,
                forgotBox,
                loginBtn,
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

        VBox inputWrapper = new VBox();
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
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

        inputWrapper.getChildren().add(passwordField);
        fieldBox.getChildren().addAll(label, inputWrapper);

        return fieldBox;
    }

    public void onLogin() {
        msgLabel.setText("");
        loadingOverlay.show("Signing in...");

        try {
            String token = authService.login(emailField.getText().trim(), passwordField.getText().trim());
            Session.setToken(token);
            String role = JwtService.getRole(token);

            loadingOverlay.updateMessage("✅ Login successful!");

            msgLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: 600;");
            msgLabel.setText("✅ Login successful!");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(0.8));
            pause.setOnFinished(e -> {
                loadingOverlay.hide();
                if ("ADMIN".equals(role))
                    HelloApplication.setRoot(new AdminProductsController());
                else
                    HelloApplication.setRoot(new CustomerHomeController());
            });
            pause.play();

        } catch (Exception e) {
            loadingOverlay.hide();
            msgLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 13px; -fx-font-weight: 600;");
            msgLabel.setText("❌ " + e.getMessage());
        }
    }

    private void onForgotPassword() {
        HelloApplication.setRoot(new ForgotPasswordController());
    }

    public void goRegister() {
        HelloApplication.setRoot(new RegisterController());
    }
}