package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.model.Order;
import com.example.finalproject.model.User;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.security.Session;
import com.example.finalproject.service.InvoiceService;
import com.example.finalproject.service.OrderService;
import com.example.finalproject.service.EmailNotificationService;
import com.example.finalproject.util.EmailSender;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Path;

import com.example.finalproject.dao.UserDao;

public class AdminOrdersController {

    private TableView<Order> orderTable;
    private TableColumn<Order, Integer> colId;
    private TableColumn<Order, Integer> colUser;
    private TableColumn<Order, Double> colTotal;
    private TableColumn<Order, String> colStatus;
    private TableColumn<Order, String> colDate;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private final UserDao userDao = new UserDao();

    private final OrderService service = new OrderService();
    private final EmailNotificationService emailNotificationService = new EmailNotificationService();

    public Parent createView() {
        AuthGuard.requireAdmin();

        BorderPane root = new BorderPane();
        root.setPrefSize(1200, 750);
        root.setStyle("-fx-background-color: #f5f7fa;");

        
        VBox topSection = new VBox();
        topSection.getChildren().addAll(createTopBar(), createFilterBar());
        root.setTop(topSection);

        
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(30));
        centerBox.setAlignment(Pos.TOP_CENTER);

        VBox tableCard = createTableCard();
        VBox actionCard = createActionCard();

        centerBox.getChildren().addAll(tableCard, actionCard);

        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(scrollPane);

        
        loadData();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("Orders Management");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = createHeaderButton("← Back to Products");
        backBtn.setOnAction(e -> goProducts());

        Button reportsBtn = createHeaderButton("📊 Reports");
        reportsBtn.setOnAction(e -> onViewReports());

        Button logoutBtn = createHeaderButton("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.5); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> logout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn, reportsBtn, logoutBtn);
        return topBar;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15, 30, 15, 30));
        filterBar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");

        searchField = new TextField();
        searchField.setPromptText("Search by Order ID or User ID...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 10; -fx-padding: 10 15; -fx-font-size: 14px;");

        Label filterIcon = new Label("🏷️");
        filterIcon.setStyle("-fx-font-size: 18px;");

        statusFilter = new ComboBox<>();
        statusFilter.setPrefWidth(150);
        statusFilter.getItems().addAll("ALL", "PENDING", "DELIVERED");
        statusFilter.setValue("ALL");
        statusFilter.setStyle("-fx-background-radius: 10; -fx-font-size: 14px;");

        Button applyBtn = createFilterButton("Apply Filter", "#667eea");
        applyBtn.setOnAction(e -> onApplyFilter());

        Button resetBtn = createFilterButton("Reset", "#6c757d");
        resetBtn.setOnAction(e -> onResetFilter());

        filterBar.getChildren().addAll(searchIcon, searchField, filterIcon, statusFilter, applyBtn, resetBtn);
        return filterBar;
    }

    private VBox createTableCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(1100);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        
        orderTable = new TableView<>();
        orderTable.setPrefHeight(450);
        orderTable.setStyle("-fx-background-color: transparent;");

        colId = new TableColumn<>("Order ID");
        colId.setPrefWidth(100);
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setCellFactory(col -> new TableCell<Order, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #667eea;");
                }
            }
        });

        colUser = new TableColumn<>("Customer ID");
        colUser.setPrefWidth(120);
        colUser.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUserId()).asObject());

        colTotal = new TableColumn<>("Total Amount");
        colTotal.setPrefWidth(150);
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotal()).asObject());
        colTotal.setCellFactory(col -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                    setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 14px;");
                }
            }
        });

        colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(150);
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;");

                    if ("DELIVERED".equalsIgnoreCase(item)) {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if ("PENDING".equalsIgnoreCase(item)) {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    } else {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colDate = new TableColumn<>("Order Date");
        colDate.setPrefWidth(280);
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCreatedAt().toString()));
        colDate.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
                }
            }
        });

        orderTable.getColumns().addAll(colId, colUser, colTotal, colStatus, colDate);

        card.getChildren().add(orderTable);
        return card;
    }

    private VBox createActionCard() {
        VBox card = new VBox(20);
        card.setMaxWidth(1100);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        Label actionsLabel = new Label("⚡ Quick Actions");
        actionsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        
        HBox primaryActions = new HBox(12);
        primaryActions.setAlignment(Pos.CENTER_LEFT);

        Button deliverBtn = createActionButton("✅ Mark Delivered", "#28a745");
        deliverBtn.setOnAction(e -> onDeliver());

        Button viewItemsBtn = createActionButton("📦 View Items", "#667eea");
        viewItemsBtn.setOnAction(e -> onViewItems());

        Button invoiceBtn = createActionButton("📄 Download Invoice", "#17a2b8");
        invoiceBtn.setOnAction(e -> onDownloadInvoice());

        Button deleteBtn = createActionButton("🗑️ Delete Order", "#dc3545");
        deleteBtn.setOnAction(e -> onDelete());




        primaryActions.getChildren().addAll(deliverBtn, viewItemsBtn, invoiceBtn, deleteBtn);


        
        HBox exportActions = new HBox(12);
        exportActions.setAlignment(Pos.CENTER_LEFT);

        Label exportLabel = new Label("📥 Export Options:");
        exportLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        Button exportCSVBtn = createActionButton("CSV Export", "#ffc107");
        exportCSVBtn.setOnAction(e -> onExportCSV());

        Button exportPDFBtn = createActionButton("PDF Export", "#6c757d");
        exportPDFBtn.setOnAction(e -> onExportPDF());

        exportActions.getChildren().addAll(exportLabel, exportCSVBtn, exportPDFBtn);

        card.getChildren().addAll(actionsLabel, primaryActions, new Separator(), exportActions);
        return card;
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

    private Button createFilterButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 15; -fx-font-weight: 600; -fx-font-size: 13px;");
        return btn;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-font-weight: 600; -fx-font-size: 14px;");

        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 10; -fx-padding: 10 20; -fx-font-weight: 600; -fx-font-size: 14px; " +
                    "-fx-opacity: 0.9; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 10; -fx-padding: 10 20; -fx-font-weight: 600; -fx-font-size: 14px;");
        });

        return btn;
    }

    private void loadData() {
        orderTable.setItems(FXCollections.observableArrayList(service.getAllOrders()));
    }

    private void onDeliver() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "Please select an order to mark as delivered.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delivery");
        confirm.setHeaderText("Mark Order as Delivered");
        confirm.setContentText("Mark Order #" + selected.getId() + " as delivered?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            service.markDelivered(selected.getId());

            
            User user = userDao.getUserById(selected.getUserId()).orElse(null);
            if (user != null) {
                
                selected.setStatus("DELIVERED");
                emailNotificationService.sendShippingNotificationEmail(user, selected);
                System.out.println("✅ Shipping notification sent to: " + user.getEmail());
            }

            loadData();
            showStyledAlert("Success", "✅ Order marked as delivered!", Alert.AlertType.INFORMATION);
        }
        try {
            
            Path pdf = new InvoiceService().generateInvoice(selected.getId());
            File file = pdf.toFile();

            
            String email = userDao.getEmailById(selected.getUserId());

            if (email == null) {
                showStyledAlert("Error", "Customer email not found!", Alert.AlertType.ERROR);
                return;
            }

            boolean sent = EmailSender.sendEmailWithAttachment(
                    email,
                    "Your Invoice for Order #" + selected.getId(),
                    "Hello,\nYour invoice is attached.\nThank you!",
                    file
            );

            if (sent) {
                showStyledAlert("Success", "Invoice emailed to: " + email, Alert.AlertType.INFORMATION);
            } else {
                showStyledAlert("Error", "Failed to send invoice email.", Alert.AlertType.ERROR);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showStyledAlert("Error", "Failed to send invoice: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void onDelete() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "Please select an order to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Order");
        confirm.setContentText("Are you sure you want to delete Order #" + selected.getId() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            service.deleteOrder(selected.getId());
            loadData();
            showStyledAlert("Success", "✅ Order deleted successfully!", Alert.AlertType.INFORMATION);
        }
    }

    private void onApplyFilter() {
        String keyword = searchField.getText().trim();
        String status = statusFilter.getValue();
        orderTable.setItems(FXCollections.observableArrayList(service.filterOrders(keyword, status)));
    }

    private void onResetFilter() {
        searchField.clear();
        statusFilter.setValue("ALL");
        loadData();
    }

    private void onViewReports() {
        HelloApplication.setRoot(new AdminReportsController());
    }

    private void onExportCSV() {
        try {
            java.io.File file = new java.io.File("orders_export.csv");
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write("OrderID,UserID,Total,Status,CreatedAt\n");
                for (var order : service.getAllOrders()) {
                    writer.write(order.getId() + "," + order.getUserId() + "," +
                            order.getTotal() + "," + order.getStatus() + "," +
                            order.getCreatedAt() + "\n");
                }
            }
            showStyledAlert("Export Successful", "✅ Orders exported to: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Export Failed", "❌ Error exporting CSV: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void onExportPDF() {
        try {
            String pdfPath = "orders_export.pdf";
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(pdfPath));
            doc.open();

            doc.add(new com.itextpdf.text.Paragraph("Orders Report"));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(5);
            table.addCell("Order ID");
            table.addCell("User ID");
            table.addCell("Total");
            table.addCell("Status");
            table.addCell("Created At");

            for (var o : service.getAllOrders()) {
                table.addCell(String.valueOf(o.getId()));
                table.addCell(String.valueOf(o.getUserId()));
                table.addCell(String.format("%.2f", o.getTotal()));
                table.addCell(o.getStatus());
                table.addCell(o.getCreatedAt().toString());
            }

            doc.add(table);
            doc.close();

            showStyledAlert("Export Successful", "✅ PDF generated at: " + pdfPath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Export Failed", "❌ Error generating PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void onDownloadInvoice() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "Please select an order first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            java.nio.file.Path pdf = new InvoiceService().generateInvoice(selected.getId());
            showStyledAlert("Success", "✅ Invoice saved to: " + pdf.toString(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Error", "❌ Failed to generate invoice: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void onViewItems() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStyledAlert("No Selection", "Please select an order first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            OrderItemsPopupController controller = new OrderItemsPopupController();
            Parent root = controller.createView();
            controller.setOrderId(selected.getId());

            Stage stage = new Stage();
            stage.setTitle("Order #" + selected.getId() + " Details");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Error", "Error loading order details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void goProducts() {
        HelloApplication.setRoot(new AdminProductsController());
    }

    private void logout() {
        Session.clear();
        HelloApplication.setRoot(new LoginController());
    }

    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-padding: 20;");

        alert.showAndWait();
    }
}