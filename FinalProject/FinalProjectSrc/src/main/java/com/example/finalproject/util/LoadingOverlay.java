package com.example.finalproject.util;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;






public class LoadingOverlay {

    private final StackPane overlay;
    private final ProgressIndicator spinner;
    private final Label messageLabel;
    private final VBox contentBox;

    public LoadingOverlay() {
        
        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.setVisible(false);
        overlay.setMouseTransparent(false); 

        
        contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(250);
        contentBox.setMaxHeight(200);
        contentBox.setPadding(new javafx.geometry.Insets(30));

        
        spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);

        
        messageLabel = new Label("Loading...");
        messageLabel.setMaxWidth(200);

        contentBox.getChildren().addAll(spinner, messageLabel);
        overlay.getChildren().add(contentBox);

        
        applyTheme();
    }

    


    private void applyTheme() {
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

    


    public StackPane getOverlay() {
        return overlay;
    }

    


    public void show() {
        show("Loading...");
    }

    


    public void show(String message) {
        applyTheme(); 
        messageLabel.setText(message);
        overlay.setVisible(true);
        overlay.toFront();

        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    


    public void hide() {
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> overlay.setVisible(false));
        fadeOut.play();
    }

    


    public void updateMessage(String message) {
        messageLabel.setText(message);
    }

    


    public boolean isShowing() {
        return overlay.isVisible();
    }
}
