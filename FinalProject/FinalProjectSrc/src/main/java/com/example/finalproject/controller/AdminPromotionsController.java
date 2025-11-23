package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.PromotionDao;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.Promotion;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.ProductService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminPromotionsController {

    private TableView<Promotion> table;
    private TableColumn<Promotion, Integer> colId;
    private TableColumn<Promotion, Integer> colProductId;
    private TableColumn<Promotion, String> colCategory;
    private TableColumn<Promotion, Double> colDiscount;
    private TableColumn<Promotion, String> colStart;
    private TableColumn<Promotion, String> colEnd;
    private Label statsLabel;
    private VBox centerContent;

    private final PromotionDao dao = new PromotionDao();

    public Parent createView() {
        AuthGuard.requireAdmin();

        BorderPane root = new BorderPane();
        root.setPrefSize(900, 650);
        root.setStyle("-fx-background-color: #f5f7fa;");

        
        HBox topBar = createTopBar();
        root.setTop(topBar);

        
        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);

        
        refresh();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setSpacing(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-padding: 20;");

        
        Label iconLabel = new Label("📢");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Manage Promotions");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        backBtn.setOnAction(e -> onBack());

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.9); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.9); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn, logoutBtn);

        return topBar;
    }

    private VBox createCenterContent() {
        centerContent = new VBox();
        centerContent.setSpacing(20);
        centerContent.setAlignment(Pos.TOP_CENTER);
        centerContent.setPadding(new Insets(30));

        
        HBox statsCard = createStatsCard();

        
        VBox contentContainer = new VBox(20);
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(25));
        contentContainer.setMaxWidth(850);
        contentContainer.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        
        Label tableTitle = new Label("🎯 Active Promotions");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        
        table = new TableView<>();
        table.setPrefHeight(330);
        table.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");

        
        colId = new TableColumn<>("ID");
        colId.setPrefWidth(60);
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colId.setStyle("-fx-alignment: CENTER;");

        colProductId = new TableColumn<>("🏷️ Product ID");
        colProductId.setPrefWidth(110);
        colProductId.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().getProductId() != null ? data.getValue().getProductId() : 0
        ).asObject());
        colProductId.setCellFactory(column -> new TableCell<Promotion, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("All");
                    setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic; -fx-alignment: CENTER;");
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: #667eea; -fx-font-weight: 600; -fx-alignment: CENTER;");
                }
            }
        });

        colCategory = new TableColumn<>("📦 Category");
        colCategory.setPrefWidth(150);
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        colCategory.setCellFactory(column -> new TableCell<Promotion, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("All");
                    setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: 600;");
                }
            }
        });

        colDiscount = new TableColumn<>("💰 Discount");
        colDiscount.setPrefWidth(120);
        colDiscount.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getDiscount()).asObject());
        colDiscount.setCellFactory(column -> new TableCell<Promotion, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.0f%%", item));
                    String badgeStyle = "-fx-background-radius: 12; -fx-padding: 5 15; " +
                            "-fx-font-weight: bold; -fx-alignment: CENTER;";
                    if (item >= 50) {
                        setStyle(badgeStyle + "-fx-background-color: #dc354520; -fx-text-fill: #dc3545;");
                    } else if (item >= 25) {
                        setStyle(badgeStyle + "-fx-background-color: #ffc10720; -fx-text-fill: #ffc107;");
                    } else {
                        setStyle(badgeStyle + "-fx-background-color: #28a74520; -fx-text-fill: #28a745;");
                    }
                }
            }
        });

        colStart = new TableColumn<>("📅 Start Date");
        colStart.setPrefWidth(140);
        colStart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));
        colStart.setStyle("-fx-alignment: CENTER;");

        colEnd = new TableColumn<>("📅 End Date");
        colEnd.setPrefWidth(140);
        colEnd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));
        colEnd.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colId, colProductId, colCategory, colDiscount, colStart, colEnd);

        
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setPadding(new Insets(10, 0, 0, 0));

        Button addBtn = new Button("➕ Add Promotion");
        addBtn.setPrefWidth(180);
        addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(40,167,69,0.4), 10, 0, 0, 4);"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;"));
        addBtn.setOnAction(e -> onAdd());

        Button deleteBtn = new Button("🗑️ Delete Selected");
        deleteBtn.setPrefWidth(180);
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,53,69,0.4), 10, 0, 0, 4);"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;"));
        deleteBtn.setOnAction(e -> onDelete());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setPrefWidth(150);
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;");
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 4);"));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 12 24; -fx-font-weight: 600; -fx-font-size: 14px;"));
        refreshBtn.setOnAction(e -> refresh());

        actionBar.getChildren().addAll(addBtn, deleteBtn, refreshBtn);

        contentContainer.getChildren().addAll(tableTitle, table, actionBar);
        centerContent.getChildren().addAll(statsCard, contentContainer);

        return centerContent;
    }

    private HBox createStatsCard() {
        HBox statsCard = new HBox(30);
        statsCard.setAlignment(Pos.CENTER);
        statsCard.setPadding(new Insets(20));
        statsCard.setMaxWidth(850);
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        statsLabel = new Label("Loading statistics...");
        statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        statsCard.getChildren().add(statsLabel);

        return statsCard;
    }

    private void updateStatsCard(List<Promotion> promotions) {
        int totalPromos = promotions.size();
        long activePromos = promotions.stream()
                .filter(p -> {
                    LocalDate now = LocalDate.now();
                    LocalDate start = p.getStartDate().toLocalDate();
                    LocalDate end = p.getEndDate().toLocalDate();
                    return !now.isBefore(start) && !now.isAfter(end);
                })
                .count();

        double avgDiscount = promotions.isEmpty() ? 0 :
                promotions.stream().mapToDouble(Promotion::getDiscount).average().orElse(0.0);

        HBox newStatsCard = new HBox(30);
        newStatsCard.setAlignment(Pos.CENTER);
        newStatsCard.setPadding(new Insets(20));
        newStatsCard.setMaxWidth(850);
        newStatsCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        VBox totalBox = createStatBox("🎯", String.valueOf(totalPromos), "Total Promotions", "#667eea");
        VBox activeBox = createStatBox("✅", String.valueOf(activePromos), "Active Now", "#28a745");
        VBox avgBox = createStatBox("💰", String.format("%.0f%%", avgDiscount), "Avg. Discount", "#ffc107");

        newStatsCard.getChildren().addAll(totalBox, activeBox, avgBox);

        
        if (centerContent != null && centerContent.getChildren().size() > 0) {
            centerContent.getChildren().set(0, newStatsCard);
        }
    }

    private VBox createStatBox(String icon, String value, String label, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "40; -fx-border-radius: 12; -fx-border-width: 1;");
        box.setPrefWidth(220);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        box.getChildren().addAll(iconLabel, valueLabel, textLabel);

        return box;
    }

    private void refresh() {
        List<Promotion> list = dao.getAll();
        table.getItems().setAll(list);
        updateStatsCard(list);
    }

    private void onAdd() {
        Dialog<Promotion> dialog = new Dialog<>();
        dialog.setTitle("Add New Promotion");
        dialog.setHeaderText("Create a new promotional offer");

        ButtonType addButtonType = new ButtonType("Add Promotion", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16;");

        
        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.getItems().addAll(new ProductService().getAll());
        productCombo.setPromptText("Select Product (optional)");
        productCombo.setPrefWidth(300);
        productCombo.setStyle("-fx-background-radius: 8;");
        productCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName() + " (#" + p.getId() + ")");
            }
        });
        productCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "All Products" : p.getName());
            }
        });

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Console", "PC", "Accessory", "Game", "Controller");
        categoryCombo.setPromptText("Select Category (optional)");
        categoryCombo.setPrefWidth(300);
        categoryCombo.setStyle("-fx-background-radius: 8;");

        Spinner<Double> discountSpinner = new Spinner<>();
        SpinnerValueFactory<Double> valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 100.0, 10.0, 5.0);
        discountSpinner.setValueFactory(valueFactory);
        discountSpinner.setEditable(true);
        discountSpinner.setPrefWidth(300);
        discountSpinner.setStyle("-fx-background-radius: 8;");

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPrefWidth(300);
        startDatePicker.setStyle("-fx-background-radius: 8;");

        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(30));
        endDatePicker.setPrefWidth(300);
        endDatePicker.setStyle("-fx-background-radius: 8;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 20, 25));

        Label productLabel = new Label("🏷️ Product:");
        productLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #495057;");
        Label categoryLabel = new Label("📦 Category:");
        categoryLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #495057;");
        Label discountLabel = new Label("💰 Discount (%):");
        discountLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #495057;");
        Label startLabel = new Label("📅 Start Date:");
        startLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #495057;");
        Label endLabel = new Label("📅 End Date:");
        endLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #495057;");

        grid.add(productLabel, 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(categoryLabel, 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(discountLabel, 0, 2);
        grid.add(discountSpinner, 1, 2);
        grid.add(startLabel, 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(endLabel, 0, 4);
        grid.add(endDatePicker, 1, 4);

        Label noteLabel = new Label("💡 Note: Leave Product and Category empty to apply to all items");
        noteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px; -fx-font-style: italic;");
        grid.add(noteLabel, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);

        
        dialog.setResultConverter(button -> {
            if (button == addButtonType) {
                try {
                    
                    if (discountSpinner.getValue() <= 0) {
                        showStyledAlert("Invalid Input", "❌ Discount must be greater than 0%", Alert.AlertType.ERROR);
                        return null;
                    }
                    if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                        showStyledAlert("Invalid Input", "❌ Please select both start and end dates.", Alert.AlertType.ERROR);
                        return null;
                    }
                    if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                        showStyledAlert("Invalid Input", "❌ End date must be after the start date.", Alert.AlertType.ERROR);
                        return null;
                    }

                    
                    Promotion p = new Promotion();
                    Product selected = productCombo.getValue();
                    if (selected != null) p.setProductId(selected.getId());
                    p.setCategory(categoryCombo.getValue());
                    p.setDiscount(discountSpinner.getValue());
                    p.setStartDate(Date.valueOf(startDatePicker.getValue()));
                    p.setEndDate(Date.valueOf(endDatePicker.getValue()));
                    return p;

                } catch (Exception ex) {
                    showStyledAlert("Error", "❌ Invalid input: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        
        dialog.showAndWait().ifPresent(promo -> {
            try {
                dao.insert(promo);
                refresh();
                showStyledAlert("Success", "✅ Promotion added successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showStyledAlert("Error", "❌ Failed to add promotion: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void onDelete() {
        Promotion selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "⚠️ Please select a promotion to delete.", Alert.AlertType.WARNING);
            return;
        }

        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this promotion?\n\n" +
                "Discount: " + String.format("%.0f%%", selected.getDiscount()) + "\n" +
                "Period: " + selected.getStartDate() + " to " + selected.getEndDate());

        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-padding: 20;");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.delete(selected.getId());
            refresh();
            showStyledAlert("Success", "✅ Promotion deleted successfully!", Alert.AlertType.INFORMATION);
        }
    }

    private void onBack() {
        HelloApplication.setRoot(new AdminProductsController());
    }

    private void onLogout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }

    private void showStyledAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-padding: 20;");

        alert.showAndWait();
    }
}