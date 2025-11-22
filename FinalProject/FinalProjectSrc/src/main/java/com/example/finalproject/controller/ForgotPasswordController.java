package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.UserDao;
import com.example.finalproject.util.EmailSender;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.Random;

public class ForgotPasswordController {

    private TextField emailField;
    private TextField otpField;
    private PasswordField newPasswordField;
    private Label messageLabel;
    private VBox otpSection;
    private Button sendOtpBtn;
    private Button resetBtn;

    private String generatedOtp;
    private String targetEmail;

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setPrefSize(900, 650);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Top bar with gradient
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center content
        StackPane centerStack = new StackPane();
        centerStack.setAlignment(Pos.CENTER);

        VBox formContainer = createFormContainer();
        centerStack.getChildren().add(formContainer);

        root.setCenter(centerStack);

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setSpacing(15);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-padding: 20;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Icon and Title
        Label iconLabel = new Label("🔐");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Password Recovery");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Login");
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

    private VBox createFormContainer() {
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        container.setMaxWidth(500);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 20; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 25, 0, 0, 10);");

        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);

        Label headerIcon = new Label("🔑");
        headerIcon.setStyle("-fx-font-size: 48px;");

        Label headerTitle = new Label("Reset Your Password");
        headerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label headerSubtitle = new Label("Enter your email to receive a verification code");
        headerSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        headerSubtitle.setWrapText(true);
        headerSubtitle.setMaxWidth(400);
        headerSubtitle.setAlignment(Pos.CENTER);

        headerSection.getChildren().addAll(headerIcon, headerTitle, headerSubtitle);

        // Email section
        VBox emailSection = new VBox(8);
        emailSection.setPrefWidth(400);

        Label emailLabel = new Label("📧 Email Address");
        emailLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #495057;");

        emailField = new TextField();
        emailField.setPromptText("Enter your registered email");
        emailField.setPrefHeight(45);
        emailField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;");
        emailField.setOnMouseEntered(e -> emailField.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;"));
        emailField.setOnMouseExited(e -> emailField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;"));

        sendOtpBtn = new Button("📤 Send Verification Code");
        sendOtpBtn.setPrefWidth(400);
        sendOtpBtn.setPrefHeight(45);
        sendOtpBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px;");
        sendOtpBtn.setOnMouseEntered(e -> sendOtpBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 4);"));
        sendOtpBtn.setOnMouseExited(e -> sendOtpBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px;"));
        sendOtpBtn.setOnAction(e -> onSendOtp());

        emailSection.getChildren().addAll(emailLabel, emailField, sendOtpBtn);

        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(400);
        separator.setStyle("-fx-padding: 10 0;");

        // OTP and Password section (initially hidden)
        otpSection = new VBox(20);
        otpSection.setPrefWidth(400);
        otpSection.setVisible(false);
        otpSection.setManaged(false);

        // OTP field
        VBox otpBox = new VBox(8);
        Label otpLabel = new Label("🔢 Verification Code");
        otpLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #495057;");

        otpField = new TextField();
        otpField.setPromptText("Enter 6-digit code");
        otpField.setPrefHeight(45);
        otpField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;");
        otpField.setOnMouseEntered(e -> otpField.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;"));
        otpField.setOnMouseExited(e -> otpField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;"));

        otpBox.getChildren().addAll(otpLabel, otpField);

        // New password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("🔒 New Password");
        passwordLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #495057;");

        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password (min 6 characters)");
        newPasswordField.setPrefHeight(45);
        newPasswordField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;");
        newPasswordField.setOnMouseEntered(e -> newPasswordField.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;"));
        newPasswordField.setOnMouseExited(e -> newPasswordField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 10 15; -fx-font-size: 14px;"));

        passwordBox.getChildren().addAll(passwordLabel, newPasswordField);

        // Reset button
        resetBtn = new Button("✅ Reset Password");
        resetBtn.setPrefWidth(400);
        resetBtn.setPrefHeight(45);
        resetBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px;");
        resetBtn.setOnMouseEntered(e -> resetBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(40,167,69,0.4), 10, 0, 0, 4);"));
        resetBtn.setOnMouseExited(e -> resetBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px;"));
        resetBtn.setOnAction(e -> onResetPassword());

        otpSection.getChildren().addAll(otpBox, passwordBox, resetBtn);

        // Message label
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10;");

        container.getChildren().addAll(
                headerSection,
                emailSection,
                separator,
                otpSection,
                messageLabel
        );

        return container;
    }

    private void onSendOtp() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showMessage("⚠️ Please enter your email address.", "warning");
            return;
        }

        UserDao dao = new UserDao();
        if (!dao.emailExists(email)) {
            showMessage("❌ No account found with that email.", "error");
            return;
        }

        generatedOtp = String.valueOf(new Random().nextInt(900000) + 100000);
        targetEmail = email;

        boolean sent = EmailSender.sendEmail(email, "Password Reset OTP",
                "Your OTP code is: " + generatedOtp);

        if (sent) {
            showMessage("✅ Verification code sent to your email!", "success");

            // Show OTP section with animation
            otpSection.setVisible(true);
            otpSection.setManaged(true);

            FadeTransition fade = new FadeTransition(Duration.millis(400), otpSection);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

            // Disable email field and send button
            emailField.setDisable(true);
            sendOtpBtn.setDisable(true);
        } else {
            showMessage("❌ Failed to send email. Please try again.", "error");
        }
    }

    private void onResetPassword() {
        if (generatedOtp == null || targetEmail == null) {
            showMessage("⚠️ Please request a verification code first.", "warning");
            return;
        }

        if (!otpField.getText().equals(generatedOtp)) {
            showMessage("❌ Invalid verification code.", "error");
            return;
        }

        String newPass = newPasswordField.getText();
        if (newPass.length() < 6) {
            showMessage("⚠️ Password must be at least 6 characters long.", "warning");
            return;
        }

        new UserDao().updatePassword(targetEmail, newPass);
        showStyledAlert("Success", "✅ Password reset successfully!\n\nYou can now login with your new password.",
                Alert.AlertType.INFORMATION);

        // Navigate back to login
        onBack();
    }

    private void showMessage(String text, String type) {
        messageLabel.setText(text);

        switch (type) {
            case "success":
                messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10; " +
                        "-fx-background-color: #d4edda; -fx-text-fill: #155724; " +
                        "-fx-background-radius: 8; -fx-border-color: #c3e6cb; -fx-border-radius: 8;");
                break;
            case "error":
                messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10; " +
                        "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; " +
                        "-fx-background-radius: 8; -fx-border-color: #f5c6cb; -fx-border-radius: 8;");
                break;
            case "warning":
                messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10; " +
                        "-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                        "-fx-background-radius: 8; -fx-border-color: #ffeeba; -fx-border-radius: 8;");
                break;
        }

        FadeTransition fade = new FadeTransition(Duration.millis(300), messageLabel);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void onBack() {
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