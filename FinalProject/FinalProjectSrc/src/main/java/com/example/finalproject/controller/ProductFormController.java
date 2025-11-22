package com.example.finalproject.controller;

import com.example.finalproject.model.Product;
import com.example.finalproject.model.Platform;
import com.example.finalproject.model.Genre;
import com.example.finalproject.service.ProductService;
import com.example.finalproject.dao.ProductDao;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductFormController {

    private Label formTitle;
    private TextField nameField, priceField, stockField;
    private TextArea descriptionArea;
    private Label msgLabel;
    private ImageView imageView;
    private ComboBox<String> categoryBox;
    private ComboBox<String> ageRatingBox;
    private ComboBox<String> productTypeBox;
    private VBox platformCheckboxContainer;
    private VBox genreCheckboxContainer;
    private Map<Integer, CheckBox> platformCheckboxes = new HashMap<>();
    private Map<Integer, CheckBox> genreCheckboxes = new HashMap<>();

    private final ProductService productService = new ProductService();
    private final ProductDao productDao = new ProductDao();
    private Product currentProduct;
    private Runnable onSaveCallback;

    private String selectedImagePath;

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setPrefSize(600, 900);
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
        categoryBox.getItems().addAll("Console", "PC", "Accessory", "Game", "Controller", "GiftCard");
        categoryBox.setStyle("-fx-font-size: 14px;");

        categoryRow.getChildren().addAll(categoryLabel, categoryBox);

        // Product Type dropdown
        HBox productTypeRow = new HBox(10);
        productTypeRow.setAlignment(Pos.CENTER_LEFT);
        Label productTypeLabel = new Label("Product Type:");
        productTypeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        productTypeBox = new ComboBox<>();
        productTypeBox.setPrefWidth(300);
        productTypeBox.setPromptText("Select Product Type");
        productTypeBox.getItems().addAll("Physical", "Digital", "GiftCard");
        productTypeBox.setValue("Physical");
        productTypeBox.setStyle("-fx-font-size: 14px;");

        productTypeRow.getChildren().addAll(productTypeLabel, productTypeBox);

        priceField = createStyledTextField("Price (USD)");
        stockField = createStyledTextField("Stock Quantity");

        // Age Rating dropdown
        HBox ageRatingRow = new HBox(10);
        ageRatingRow.setAlignment(Pos.CENTER_LEFT);
        Label ageRatingLabel = new Label("Age Rating:");
        ageRatingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        ageRatingBox = new ComboBox<>();
        ageRatingBox.setPrefWidth(300);
        ageRatingBox.setPromptText("Select Age Rating (Optional)");
        ageRatingBox.getItems().addAll("", "E", "E10+", "T", "M", "AO", "PEGI 3", "PEGI 7", "PEGI 12", "PEGI 16", "PEGI 18");
        ageRatingBox.setStyle("-fx-font-size: 14px;");

        ageRatingRow.getChildren().addAll(ageRatingLabel, ageRatingBox);

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Product Description");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 12; -fx-font-size: 14px;");

        // Platforms section
        VBox platformsSection = createPlatformsSection();

        // Genres section
        VBox genresSection = createGenresSection();

        section.getChildren().addAll(nameField, categoryRow, productTypeRow, priceField, stockField,
                                     ageRatingRow, descriptionArea, platformsSection, genresSection);
        return section;
    }

    private VBox createPlatformsSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 15;");

        Label title = new Label("🎮 Platforms (Select all that apply)");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        platformCheckboxContainer = new VBox(5);

        // Load platforms from database
        List<Platform> platforms = productDao.getAllPlatforms();
        for (Platform platform : platforms) {
            CheckBox cb = new CheckBox(platform.getName());
            cb.setStyle("-fx-font-size: 13px;");
            platformCheckboxes.put(platform.getId(), cb);
            platformCheckboxContainer.getChildren().add(cb);
        }

        section.getChildren().addAll(title, platformCheckboxContainer);
        return section;
    }

    private VBox createGenresSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-padding: 15;");

        Label title = new Label("🎯 Genres (Select all that apply)");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        genreCheckboxContainer = new VBox(5);

        // Load genres from database
        List<Genre> genres = productDao.getAllGenres();
        for (Genre genre : genres) {
            CheckBox cb = new CheckBox(genre.getName());
            cb.setStyle("-fx-font-size: 13px;");
            genreCheckboxes.put(genre.getId(), cb);
            genreCheckboxContainer.getChildren().add(cb);
        }

        section.getChildren().addAll(title, genreCheckboxContainer);
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

            // Set product type
            if (product.getProductType() != null && !product.getProductType().isEmpty()) {
                productTypeBox.setValue(product.getProductType());
            } else {
                productTypeBox.setValue("Physical");
            }

            // Set age rating
            if (product.getAgeRating() != null && !product.getAgeRating().isEmpty()) {
                ageRatingBox.setValue(product.getAgeRating());
            }

            // Set platform checkboxes
            if (product.getPlatformIds() != null) {
                for (Integer platformId : product.getPlatformIds()) {
                    CheckBox cb = platformCheckboxes.get(platformId);
                    if (cb != null) {
                        cb.setSelected(true);
                    }
                }
            }

            // Set genre checkboxes
            if (product.getGenreIds() != null) {
                for (Integer genreId : product.getGenreIds()) {
                    CheckBox cb = genreCheckboxes.get(genreId);
                    if (cb != null) {
                        cb.setSelected(true);
                    }
                }
            }

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

            // Collect product type
            String productType = productTypeBox.getValue();
            if (productType == null || productType.isEmpty()) {
                productType = "Physical";
            }

            // Collect age rating
            String ageRating = ageRatingBox.getValue();
            if (ageRating != null && ageRating.isEmpty()) {
                ageRating = null;
            }

            // Collect selected platforms
            List<Integer> selectedPlatformIds = new ArrayList<>();
            for (Map.Entry<Integer, CheckBox> entry : platformCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedPlatformIds.add(entry.getKey());
                }
            }

            // Collect selected genres
            List<Integer> selectedGenreIds = new ArrayList<>();
            for (Map.Entry<Integer, CheckBox> entry : genreCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedGenreIds.add(entry.getKey());
                }
            }

            if (currentProduct == null) {
                Product newP = new Product(0, name, category, price, desc, selectedImagePath, stock);
                newP.setProductType(productType);
                newP.setAgeRating(ageRating);
                newP.setPlatformIds(selectedPlatformIds);
                newP.setGenreIds(selectedGenreIds);
                productService.add(newP);
                showMessage("✅ Product added successfully!", true);
            } else {
                currentProduct.setName(name);
                currentProduct.setCategory(category);
                currentProduct.setDescription(desc);
                currentProduct.setPrice(price);
                currentProduct.setStock(stock);
                currentProduct.setImagePath(selectedImagePath);
                currentProduct.setProductType(productType);
                currentProduct.setAgeRating(ageRating);
                currentProduct.setPlatformIds(selectedPlatformIds);
                currentProduct.setGenreIds(selectedGenreIds);
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