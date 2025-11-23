package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.model.Product;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class AdminProductsController {

    private TableView<Product> table;
    private TableColumn<Product, Integer> colId;
    private TableColumn<Product, String> colName;
    private TableColumn<Product, String> colCategory;
    private TableColumn<Product, Double> colPrice;
    private TableColumn<Product, Integer> colStock;
    private TableColumn<Product, String> colImage;
    private Label msgLabel;
    private TextField searchField;

    private final ProductService productService = new ProductService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    public Parent createView() {
        AuthGuard.requireAdmin();

        BorderPane root = new BorderPane();
        root.setPrefSize(1200, 750);
        root.setStyle("-fx-background-color: #f5f7fa;");

        
        VBox topSection = new VBox();
        topSection.getChildren().addAll(createTopBar(), createToolbar());
        root.setTop(topSection);

        
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.TOP_CENTER);

        
        VBox tableCard = new VBox(15);
        tableCard.setMaxWidth(1100);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");

        searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-padding: 10 15; -fx-font-size: 14px;");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        searchBar.getChildren().addAll(searchIcon, searchField);

        
        table = new TableView<>();
        table.setPrefHeight(500);
        table.setStyle("-fx-background-color: transparent;");

        colId = new TableColumn<>("ID");
        colId.setPrefWidth(60);
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colImage = new TableColumn<>("Image");
        colImage.setPrefWidth(100);
        colImage.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getImagePath()));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    imageView.setFitWidth(70);
                    imageView.setFitHeight(70);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-background-radius: 8;");
                    imageView.setImage(new javafx.scene.image.Image("file:" + path, 70, 70, true, true));
                    setGraphic(imageView);
                }
            }
        });

        colName = new TableColumn<>("Product Name");
        colName.setPrefWidth(250);
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colName.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        colCategory = new TableColumn<>("Category");
        colCategory.setPrefWidth(150);
        colCategory.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));

        colPrice = new TableColumn<>("Price");
        colPrice.setPrefWidth(120);
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        colPrice.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                    setStyle("-fx-text-fill: #667eea; -fx-font-weight: bold;");
                }
            }
        });

        colStock = new TableColumn<>("Stock");
        colStock.setPrefWidth(100);
        colStock.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colStock.setCellFactory(col -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    if (item < 10) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else if (item < 20) {
                        setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colImage, colName, colCategory, colPrice, colStock);
        table.setItems(productList);

        
        msgLabel = new Label();
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");

        tableCard.getChildren().addAll(searchBar, table, msgLabel);
        centerBox.getChildren().add(tableCard);

        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(scrollPane);

        
        refresh();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("🛒");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("Product Management");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createHeaderButton("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.5); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, logoutBtn);
        return topBar;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(15, 30, 15, 30));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        Button addBtn = createToolbarButton("➕ Add Product", "#28a745");
        addBtn.setOnAction(e -> onAdd());

        Button editBtn = createToolbarButton("✏️ Edit", "#667eea");
        editBtn.setOnAction(e -> onEdit());

        Button deleteBtn = createToolbarButton("🗑️ Delete", "#dc3545");
        deleteBtn.setOnAction(e -> onDelete());

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setPrefHeight(30);

        Button reviewsBtn = createToolbarButton("⭐ Reviews", "#ffc107");
        reviewsBtn.setOnAction(e -> onManageReviews());

        Button promotionsBtn = createToolbarButton("💸 Promotions", "#17a2b8");
        promotionsBtn.setOnAction(e -> onPromotions());

        Button ordersBtn = createToolbarButton("📦 Orders", "#6c757d");
        ordersBtn.setOnAction(e -> onOrders());

        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, sep1, reviewsBtn, promotionsBtn, ordersBtn);
        return toolbar;
    }

    private Button createToolbarButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 15; -fx-font-weight: 600; -fx-font-size: 13px;");

        String hoverColor = adjustBrightness(color, -20);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 15; -fx-font-weight: 600; -fx-font-size: 13px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 15; -fx-font-weight: 600; -fx-font-size: 13px;"));
        return btn;
    }

    private Button createHeaderButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        return btn;
    }

    private String adjustBrightness(String hex, int amount) {
        
        return hex; 
    }

    private void filterTable(String query) {
        if (query == null || query.trim().isEmpty()) {
            table.setItems(productList);
            return;
        }

        String lowerQuery = query.toLowerCase();
        ObservableList<Product> filtered = FXCollections.observableArrayList();

        for (Product p : productList) {
            if (p.getName().toLowerCase().contains(lowerQuery) ||
                    p.getCategory().toLowerCase().contains(lowerQuery) ||
                    String.valueOf(p.getId()).contains(lowerQuery)) {
                filtered.add(p);
            }
        }

        table.setItems(filtered);
    }

    private void refresh() {
        List<Product> products = productService.getAll();
        productList.setAll(products);
        table.setItems(productList);
        msgLabel.setText("");
    }

    private void onAdd() {
        openProductForm(null);
    }

    private void onEdit() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Please select a product to edit.", false);
            return;
        }
        openProductForm(selected);
    }

    private void onDelete() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Please select a product to delete.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Product");
        confirm.setContentText("Are you sure you want to delete: " + selected.getName() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                productService.delete(selected.getId());
                refresh();
                showMessage("✅ Product deleted successfully!", true);
            } catch (Exception e) {
                showMessage("❌ " + e.getMessage(), false);
            }
        }
    }

    private void openProductForm(Product product) {
        try {
            ProductFormController controller = new ProductFormController();
            Parent root = controller.createView();
            controller.setProduct(product);
            controller.setOnSaveCallback(this::refresh);

            Stage stage = new Stage();
            stage.setTitle(product == null ? "Add Product" : "Edit Product");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message, boolean success) {
        msgLabel.setStyle("-fx-text-fill: " + (success ? "#28a745" : "#dc3545") + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        msgLabel.setText(message);
    }

    private void onManageReviews() {
        HelloApplication.setRoot(new AdminReviewsController());
    }

    private void onOrders() {
        HelloApplication.setRoot(new AdminOrdersController());
    }

    private void onPromotions() {
        HelloApplication.setRoot(new AdminPromotionsController());
    }

    private void onLogout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }
}