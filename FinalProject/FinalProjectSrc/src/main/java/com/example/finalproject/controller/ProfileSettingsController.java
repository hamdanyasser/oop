package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.DBConnection;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class ProfileSettingsController {

    private TextField nameField;
    private TextField emailField;
    private TextField addressField;
    private PasswordField oldPassField;
    private PasswordField newPassField;
    private Label msgLabel;

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
        centerBox.setPadding(new Insets(40, 20, 40, 20));

        // Profile header
        VBox headerBox = createHeaderBox();

        // Profile card
        VBox profileCard = createProfileCard();

        // Security card
        VBox securityCard = createSecurityCard();

        // Preferences card
        VBox preferencesCard = createPreferencesCard();

        centerBox.getChildren().addAll(headerBox, profileCard, securityCard, preferencesCard);
        scrollPane.setContent(centerBox);
        root.setCenter(scrollPane);

        loadUserData();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("👤");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Profile Settings");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

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
        logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn, logoutBtn);

        return topBar;
    }

    private VBox createHeaderBox() {
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);

        Label avatarLabel = new Label("👨‍💼");
        avatarLabel.setStyle("-fx-font-size: 72px; -fx-background-color: white; " +
                "-fx-padding: 20; -fx-background-radius: 50%; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);");

        Label welcomeLabel = new Label("Manage Your Profile");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");

        headerBox.getChildren().addAll(avatarLabel, welcomeLabel);

        return headerBox;
    }

    private VBox createProfileCard() {
        VBox card = new VBox(20);
        card.setMaxWidth(600);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Section header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📝");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label sectionLabel = new Label("Personal Information");
        sectionLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, sectionLabel);

        // Name field
        VBox nameBox = createInputField("Full Name", "name");
        nameField = (TextField) ((HBox) nameBox.getChildren().get(1)).getChildren().get(0);

        // Email field (read-only)
        VBox emailBox = createInputField("Email Address", "email");
        emailField = (TextField) ((HBox) emailBox.getChildren().get(1)).getChildren().get(0);
        emailField.setEditable(false);
        emailField.setStyle(emailField.getStyle() + " -fx-opacity: 0.7;");

        // Address field
        VBox addressBox = createInputField("Address", "address");
        addressField = (TextField) ((HBox) addressBox.getChildren().get(1)).getChildren().get(0);

        card.getChildren().addAll(header, nameBox, emailBox, addressBox);

        return card;
    }

    private VBox createSecurityCard() {
        VBox card = new VBox(20);
        card.setMaxWidth(600);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Section header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🔒");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label sectionLabel = new Label("Security Settings");
        sectionLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, sectionLabel);

        // Old password
        VBox oldPassBox = createPasswordField("Current Password", "oldpass");
        oldPassField = (PasswordField) ((HBox) oldPassBox.getChildren().get(1)).getChildren().get(0);

        // New password
        VBox newPassBox = createPasswordField("New Password (optional)", "newpass");
        newPassField = (PasswordField) ((HBox) newPassBox.getChildren().get(1)).getChildren().get(0);

        // Password hint
        Label hintLabel = new Label("💡 Leave new password blank if you don't want to change it");
        hintLabel.setWrapText(true);
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-padding: 0 0 0 10;");

        // Save button
        Button saveBtn = new Button("💾 Save Changes");
        saveBtn.setPrefWidth(200);
        saveBtn.setPrefHeight(45);
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 12, 0, 0, 4);");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #5568d3, #6a3f8f); " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.6), 15, 0, 0, 6);"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 12, 0, 0, 4);"));
        saveBtn.setOnAction(e -> onSave());

        // Message label
        msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(540);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(saveBtn);

        card.getChildren().addAll(header, oldPassBox, newPassBox, hintLabel, buttonBox, msgLabel);

        return card;
    }

    private VBox createPreferencesCard() {
        VBox card = new VBox(20);
        card.setMaxWidth(600);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Section header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("⚙️");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label sectionLabel = new Label("Appearance Preferences");
        sectionLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, sectionLabel);

        // Dark mode toggle
        HBox themeRow = new HBox(20);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        themeRow.setPadding(new Insets(10));
        themeRow.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1;");

        VBox themeInfo = new VBox(5);
        Label themeLabel = new Label("🌙 Dark Mode");
        themeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label themeDesc = new Label("Toggle between light and dark theme");
        themeDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        themeInfo.getChildren().addAll(themeLabel, themeDesc);
        HBox.setHgrow(themeInfo, Priority.ALWAYS);

        // Toggle button
        ThemeManager themeManager = ThemeManager.getInstance();
        Button toggleBtn = new Button(themeManager.isDarkMode() ? "🌞 Light" : "🌙 Dark");
        toggleBtn.setPrefWidth(100);
        toggleBtn.setPrefHeight(40);
        updateToggleButtonStyle(toggleBtn, themeManager.isDarkMode());

        toggleBtn.setOnAction(e -> {
            themeManager.toggleTheme();
            toggleBtn.setText(themeManager.isDarkMode() ? "🌞 Light" : "🌙 Dark");
            updateToggleButtonStyle(toggleBtn, themeManager.isDarkMode());
        });

        themeRow.getChildren().addAll(themeInfo, toggleBtn);

        // Info message
        Label infoLabel = new Label("💡 Theme preference is saved automatically and will persist across sessions");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-padding: 0 0 0 10;");

        card.getChildren().addAll(header, themeRow, infoLabel);

        return card;
    }

    private void updateToggleButtonStyle(Button btn, boolean isDarkMode) {
        if (isDarkMode) {
            btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(44,62,80,0.4), 8, 0, 0, 2);");
        } else {
            btn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(243,156,18,0.4), 8, 0, 0, 2);");
        }
    }

    private VBox createInputField(String labelText, String id) {
        VBox fieldBox = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        HBox inputBox = new HBox();
        TextField field = new TextField();
        field.setId(id);
        field.setPrefHeight(45);
        field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12 15; -fx-font-size: 14px;");
        HBox.setHgrow(field, Priority.ALWAYS);

        field.focusedProperty().addListener((obs, was, is) -> {
            if (is) {
                field.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px;");
            } else {
                field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px;");
            }
        });

        inputBox.getChildren().add(field);
        fieldBox.getChildren().addAll(label, inputBox);

        return fieldBox;
    }

    private VBox createPasswordField(String labelText, String id) {
        VBox fieldBox = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        HBox inputBox = new HBox();
        PasswordField field = new PasswordField();
        field.setId(id);
        field.setPrefHeight(45);
        field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12 15; -fx-font-size: 14px;");
        HBox.setHgrow(field, Priority.ALWAYS);

        field.focusedProperty().addListener((obs, was, is) -> {
            if (is) {
                field.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-color: #667eea; -fx-border-radius: 10; -fx-border-width: 2; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px;");
            } else {
                field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                        "-fx-padding: 12 15; -fx-font-size: 14px;");
            }
        });

        inputBox.getChildren().add(field);
        fieldBox.getChildren().addAll(label, inputBox);

        return fieldBox;
    }

    private void loadUserData() {
        try {
            int userId = JwtService.getUserId(Session.getToken());
            Connection conn = DBConnection.getInstance();
            PreparedStatement ps = conn.prepareStatement("SELECT name, email, address FROM users WHERE id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                addressField.setText(rs.getString("address"));
            }
        } catch (Exception e) {
            showMessage("⚠️ Error loading profile data.", false);
            e.printStackTrace();
        }
    }

    private void onSave() {
        try {
            int userId = JwtService.getUserId(Session.getToken());
            Connection conn = DBConnection.getInstance();

            PreparedStatement check = conn.prepareStatement("SELECT password_hash FROM users WHERE id=?");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                showMessage("❌ User not found.", false);
                return;
            }

            String oldHash = rs.getString("password_hash");
            if (!BCrypt.checkpw(oldPassField.getText(), oldHash)) {
                showMessage("❌ Incorrect current password.", false);
                return;
            }

            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String newPass = newPassField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                showMessage("⚠️ Name, email and address are required.", false);
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showMessage("⚠️ Invalid email format.", false);
                return;
            }

            if (!newPass.isEmpty()) {
                if (newPass.length() < 8 || !newPass.matches(".*[A-Z].*") ||
                        !newPass.matches(".*[a-z].*") || !newPass.matches(".*\\d.*") ||
                        !newPass.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                    showMessage("⚠️ Password must meet security requirements.", false);
                    return;
                }
            }

            String sql;
            PreparedStatement ps;
            if (newPass.isEmpty()) {
                sql = "UPDATE users SET name=?, email=?, address=? WHERE id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, address);
                ps.setInt(4, userId);
            } else {
                sql = "UPDATE users SET name=?, email=?, address=?, password_hash=? WHERE id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, address);
                ps.setString(4, BCrypt.hashpw(newPass, BCrypt.gensalt()));
                ps.setInt(5, userId);
            }

            int updated = ps.executeUpdate();
            if (updated > 0) {
                showMessage("✅ Profile updated successfully!", true);
                oldPassField.clear();
                newPassField.clear();
            } else {
                showMessage("⚠️ No changes detected.", false);
            }

        } catch (Exception e) {
            showMessage("⚠️ Error saving changes: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void showMessage(String message, boolean success) {
        msgLabel.setStyle("-fx-text-fill: " + (success ? "#28a745" : "#dc3545") + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        msgLabel.setText(message);
    }

    private void onBack() {
        HelloApplication.setRoot(new CustomerHomeController());
    }

    private void onLogout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }
}