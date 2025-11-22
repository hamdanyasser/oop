package com.example.finalproject.util;

import com.example.finalproject.service.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Reusable loading overlay with spinner
 * Shows a semi-transparent overlay with loading spinner and message
 * Adapts to light/dark theme automatically
 */
public class LoadingOverlay {

    private final StackPane overlay;
    private final ProgressIndicator spinner;
    private final Label messageLabel;
    private final VBox contentBox;

    public LoadingOverlay() {
        // Create overlay
        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.setVisible(false);
        overlay.setMouseTransparent(false); // Block clicks while loading

        // Create content box
        contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(250);
        contentBox.setMaxHeight(200);
        contentBox.setPadding(new javafx.geometry.Insets(30));

        // Create spinner
        spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);

        // Create message label
        messageLabel = new Label("Loading...");
        messageLabel.setMaxWidth(200);

        contentBox.getChildren().addAll(spinner, messageLabel);
        overlay.getChildren().add(contentBox);

        // Apply initial theme
        applyTheme();
    }

    /**
     * Apply theme-appropriate styling
     */
    private void applyTheme() {
        boolean isDark = ThemeManager.getInstance().isDarkMode();

        if (isDark) {
            // Dark theme styling
            contentBox.setStyle("-fx-background-color: #2d2d2d; " +
                    "-fx-background-radius: 16; " +
                    "-fx-border-color: #404040; " +
                    "-fx-border-radius: 16; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);");

            spinner.setStyle("-fx-progress-color: #4a9eff;");

            messageLabel.setStyle("-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #e0e0e0; " +
                    "-fx-wrap-text: true; " +
                    "-fx-text-alignment: center;");
        } else {
            // Light theme styling
            contentBox.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

            spinner.setStyle("-fx-progress-color: #667eea;");

            messageLabel.setStyle("-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #2c3e50; " +
                    "-fx-wrap-text: true; " +
                    "-fx-text-alignment: center;");
        }
    }

    /**
     * Get the overlay pane (add this to your layout)
     */
    public StackPane getOverlay() {
        return overlay;
    }

    /**
     * Show loading overlay with default message
     */
    public void show() {
        show("Loading...");
    }

    /**
     * Show loading overlay with custom message
     */
    public void show(String message) {
        applyTheme(); // Refresh theme before showing
        messageLabel.setText(message);
        overlay.setVisible(true);
        overlay.toFront();

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Hide loading overlay
     */
    public void hide() {
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> overlay.setVisible(false));
        fadeOut.play();
    }

    /**
     * Update the loading message while spinner is shown
     */
    public void updateMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Check if overlay is currently visible
     */
    public boolean isShowing() {
        return overlay.isVisible();
    }
}
