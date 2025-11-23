package com.example.finalproject;

import com.example.finalproject.controller.LoginController;
import com.example.finalproject.service.ThemeManager;
import com.example.finalproject.util.ToastNotification;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        setRoot(new LoginController());
        stage.setTitle("E-Commerce App");
        stage.show();
    }

    public static void setRoot(Object controller) {
        try {
            Parent root = null;

            if (controller instanceof LoginController) {
                root = ((LoginController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.RegisterController) {
                root = ((com.example.finalproject.controller.RegisterController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.ForgotPasswordController) {
                root = ((com.example.finalproject.controller.ForgotPasswordController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.CustomerHomeController) {
                root = ((com.example.finalproject.controller.CustomerHomeController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.CartController) {
                root = ((com.example.finalproject.controller.CartController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.CheckoutController) {
                root = ((com.example.finalproject.controller.CheckoutController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.OrderHistoryController) {
                root = ((com.example.finalproject.controller.OrderHistoryController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.WishlistController) {
                root = ((com.example.finalproject.controller.WishlistController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.ProfileSettingsController) {
                root = ((com.example.finalproject.controller.ProfileSettingsController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.ProductReviewsController) {
                root = ((com.example.finalproject.controller.ProductReviewsController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.AdminProductsController) {
                root = ((com.example.finalproject.controller.AdminProductsController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.AdminOrdersController) {
                root = ((com.example.finalproject.controller.AdminOrdersController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.AdminReviewsController) {
                root = ((com.example.finalproject.controller.AdminReviewsController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.AdminReportsController) {
                root = ((com.example.finalproject.controller.AdminReportsController) controller).createView();
            } else if (controller instanceof com.example.finalproject.controller.AdminPromotionsController) {
                root = ((com.example.finalproject.controller.AdminPromotionsController) controller).createView();
            }

            if (root != null) {
                
                Parent finalRoot = root;
                if (!(root instanceof StackPane)) {
                    StackPane wrapper = new StackPane(root);
                    finalRoot = wrapper;
                }

                Scene scene = new Scene(finalRoot);
                
                ThemeManager.getInstance().setScene(scene);
                
                ToastNotification.initialize(scene);
                mainStage.setScene(scene);
                mainStage.centerOnScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
