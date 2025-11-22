package com.example.finalproject.security;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.controller.LoginController;
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
}
