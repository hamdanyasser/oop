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

    



    public static void requireAdmin() {
        
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
