package com.example.finalproject.controller;

import com.example.finalproject.model.Product;
import com.example.finalproject.service.ProductService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProductFormController {

    private Label formTitle;
    private TextField nameField, priceField, stockField;
    private TextArea descriptionArea;
    private Label msgLabel;
    private ImageView imageView;
    private ComboBox<String> categoryBox;

    private final ProductService productService = new ProductService();
    private Product currentProduct;
    private Runnable onSaveCallback;

    private String selectedImagePath;

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setPrefSize(550, 700);
        root.setStyle("-fx-background-color: white;");

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 20, 20, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 40px;");

        formTitle = new Label("Add Product");
        formTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        header.getChildren().addAll(iconLabel, formTitle);
        root.setTop(header);

        // Form content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");

        VBox formBox = new VBox(20);
        formBox.setPadding(new Insets(30));
        formBox.setAlignment(Pos.TOP_CENTER);

        // Image section
        VBox imageSection = createImageSection();

        // Form fields
        VBox fieldsSection = createFieldsSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        // Message label
        msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(450);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");

        formBox.getChildren().addAll(imageSection, fieldsSection, buttonBox, msgLabel);
        scrollPane.setContent(formBox);
        root.setCenter(scrollPane);

        return root;
    }

    private VBox createImageSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 12; -fx-border-width: 1;");

        imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 12;");

        Button chooseBtn = new Button("📂 Choose Image");
        chooseBtn.setPrefWidth(180);
        chooseBtn.setPrefHeight(40);
        chooseBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        chooseBtn.setOnAction(e -> onChooseImage());

        section.getChildren().addAll(imageView, chooseBtn);
        return section;
    }

    private VBox createFieldsSection() {
        VBox section = new VBox(15);

        nameField = createStyledTextField("Product Name");

        HBox categoryRow = new HBox(10);
        categoryRow.setAlignment(Pos.CENTER_LEFT);
        Label categoryLabel = new Label("Category:");
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        categoryBox = new ComboBox<>();
        categoryBox.setPrefWidth(300);
        categoryBox.setPromptText("Select Category");
        categoryBox.getItems().addAll("Console", "PC", "Accessory", "Game", "Controller");
        categoryBox.setStyle("-fx-font-size: 14px;");

        categoryRow.getChildren().addAll(categoryLabel, categoryBox);

        priceField = createStyledTextField("Price (USD)");
        stockField = createStyledTextField("Stock Quantity");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Product Description");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12; -fx-font-size: 14px;");

        section.getChildren().addAll(nameField, categoryRow, priceField, stockField, descriptionArea);
        return section;
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(45);
        field.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12 15; -fx-font-size: 14px;");

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

        return field;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button saveBtn = new Button("💾 Save Product");
        saveBtn.setPrefWidth(210);
        saveBtn.setPrefHeight(45);
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #28a745, #20c997); " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10;");
        saveBtn.setOnAction(e -> onSave());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(210);
        cancelBtn.setPrefHeight(45);
        cancelBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> onCancel());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        return buttonBox;
    }

    public void setProduct(Product product) {
        this.currentProduct = product;

        if (product != null) {
            formTitle.setText("Edit Product");
            nameField.setText(product.getName());
            categoryBox.setValue(product.getCategory());
            priceField.setText(String.valueOf(product.getPrice()));
            descriptionArea.setText(product.getDescription());
            stockField.setText(String.valueOf(product.getStock()));
            selectedImagePath = product.getImagePath();

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                imageView.setImage(new Image("file:" + selectedImagePath));
            }
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) uploadsDir.mkdirs();

                Path destination = Path.of("uploads", file.getName());
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                selectedImagePath = destination.toAbsolutePath().toString();
                imageView.setImage(new Image("file:" + selectedImagePath));
                showMessage("✅ Image selected successfully!", true);
            } catch (Exception e) {
                showMessage("❌ Error: " + e.getMessage(), false);
            }
        }
    }

    private void onSave() {
        try {
            String name = nameField.getText().trim();
            String category = categoryBox.getValue();
            String desc = descriptionArea.getText().trim();

            if (name.isEmpty() || category == null || category.isEmpty()) {
                showMessage("⚠️ Name and category are required.", false);
                return;
            }

            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            if (currentProduct == null) {
                Product newP = new Product(0, name, category, price, desc, selectedImagePath, stock);
                productService.add(newP);
                showMessage("✅ Product added successfully!", true);
            } else {
                currentProduct.setName(name);
                currentProduct.setCategory(category);
                currentProduct.setDescription(desc);
                currentProduct.setPrice(price);
                currentProduct.setStock(stock);
                currentProduct.setImagePath(selectedImagePath);
                productService.update(currentProduct);
                showMessage("✅ Product updated successfully!", true);
            }

            if (onSaveCallback != null) onSaveCallback.run();

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(e -> ((Stage) nameField.getScene().getWindow()).close());
            pause.play();

        } catch (NumberFormatException e) {
            showMessage("⚠️ Invalid number format in price or stock.", false);
        } catch (Exception e) {
            showMessage("❌ " + e.getMessage(), false);
        }
    }

    private void onCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showMessage(String message, boolean success) {
        msgLabel.setStyle("-fx-text-fill: " + (success ? "#28a745" : "#dc3545") + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        msgLabel.setText(message);
    }
}