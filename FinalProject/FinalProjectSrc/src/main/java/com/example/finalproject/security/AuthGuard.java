package com.example.finalproject.security;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.controller.LoginController;
import com.example.finalproject.controller.CustomerHomeController;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AuthGuard {
    public static void requireLogin() {
        if (!Session.isAuthenticated()) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText(null);
                a.setContentText("Your session expired. Please login again.");
                a.showAndWait();
                HelloApplication.setRoot(new LoginController());
            });
        }
    }

    /**
     * Require admin role - prevents privilege escalation
     * CRITICAL: Call this in all admin controllers!
     */
    public static void requireAdmin() {
        // First check if logged in
        if (!Session.isAuthenticated()) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText(null);
                a.setContentText("Your session expired. Please login again.");
                a.showAndWait();
                HelloApplication.setRoot(new LoginController());
            });
            return;
        }

        // Then check if user has admin role
        String token = Session.getToken();
        String role = JwtService.getRole(token);

        if (!"ADMIN".equals(role)) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Access Denied");
                a.setHeaderText("Unauthorized Access");
                a.setContentText("You do not have permission to access this page.\nThis incident will be logged.");
                a.showAndWait();
                HelloApplication.setRoot(new CustomerHomeController());
            });
        }
    }
}
