package com.example.finalproject.util;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;






public class ToastNotification {

    public enum Type {
        SUCCESS("✅", "#28a745", "#d4edda", "#155724"),
        ERROR("❌", "#dc3545", "#f8d7da", "#721c24"),
        INFO("ℹ️", "#17a2b8", "#d1ecf1", "#0c5460"),
        WARNING("⚠️", "#ffc107", "#fff3cd", "#856404");

        final String icon;
        final String color;
        final String bgLight;
        final String textLight;

        Type(String icon, String color, String bgLight, String textLight) {
            this.icon = icon;
            this.color = color;
            this.bgLight = bgLight;
            this.textLight = textLight;
        }
    }

    private static VBox toastContainer;
    private static final int MAX_TOASTS = 5;
    private static final Duration SHOW_DURATION = Duration.seconds(3);

    


    public static void initialize(Scene scene) {
        if (toastContainer == null) {
            toastContainer = new VBox(10);
            toastContainer.setAlignment(Pos.TOP_RIGHT);
            toastContainer.setPadding(new Insets(20));
            toastContainer.setMouseTransparent(true);
            toastContainer.setPickOnBounds(false);

            
            if (scene.getRoot() instanceof StackPane) {
                StackPane root = (StackPane) scene.getRoot();
                root.getChildren().add(toastContainer);
                toastContainer.toFront();
            }
        }
    }

    


    public static void success(String message) {
        show(message, Type.SUCCESS);
    }

    


    public static void error(String message) {
        show(message, Type.ERROR);
    }

    


    public static void info(String message) {
        show(message, Type.INFO);
    }

    


    public static void warning(String message) {
        show(message, Type.WARNING);
    }

    


    public static void show(String message, Type type) {
        if (toastContainer == null) {
            System.err.println("ToastNotification not initialized. Call initialize(scene) first.");
            return;
        }

        
        if (toastContainer.getChildren().size() >= MAX_TOASTS) {
            toastContainer.getChildren().remove(0);
        }

        
        HBox toast = createToast(message, type);
        toast.setMouseTransparent(false);
        toast.setPickOnBounds(true);

        
        toastContainer.getChildren().add(toast);

        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setFromX(400);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);
        showTransition.play();

        
        PauseTransition delay = new PauseTransition(SHOW_DURATION);
        delay.setOnFinished(e -> dismiss(toast));
        delay.play();

        
        toast.setOnMouseClicked(e -> {
            delay.stop();
            dismiss(toast);
        });
    }

    


    private static HBox createToast(String message, Type type) {
        HBox toast = new HBox(12);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(12, 16, 12, 16));
        toast.setMaxWidth(350);
        toast.setMinHeight(50);


        toast.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
            "-fx-cursor: hand;",
            type.bgLight,
            type.color
        ));


        Label iconLabel = new Label(type.icon);
        iconLabel.setStyle("-fx-font-size: 20px;");


        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);

        messageLabel.setStyle(String.format(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-text-fill: %s;",
            type.textLight
        ));

        toast.getChildren().addAll(iconLabel, messageLabel);

        return toast;
    }

    


    private static void dismiss(HBox toast) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), toast);
        slideOut.setToX(400);

        ParallelTransition hideTransition = new ParallelTransition(fadeOut, slideOut);
        hideTransition.setOnFinished(e -> toastContainer.getChildren().remove(toast));
        hideTransition.play();
    }

    


    public static void clearAll() {
        if (toastContainer != null) {
            toastContainer.getChildren().clear();
        }
    }
}
