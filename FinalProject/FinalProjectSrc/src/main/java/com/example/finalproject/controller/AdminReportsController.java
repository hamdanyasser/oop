package com.example.finalproject.controller;

import com.example.finalproject.HelloApplication;
import com.example.finalproject.dao.ReportDao;
import com.example.finalproject.model.TopProduct;
import com.example.finalproject.model.CategorySale;
import com.example.finalproject.security.AuthGuard;
import com.example.finalproject.util.LoadingOverlay;
import com.example.finalproject.util.ToastNotification;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import com.example.finalproject.security.Session;
import javafx.scene.layout.*;

import java.io.FileWriter;
import java.util.List;

public class AdminReportsController {

    private Label totalRevenueLabel;
    private Label totalOrdersLabel;
    private Label totalCustomersLabel;
    private BarChart<String, Number> salesChart;
    private LineChart<String, Number> revenueLineChart;
    private PieChart categoryPieChart;
    private TableView<TopProduct> topProductsTable;
    private TableColumn<TopProduct, String> colProduct;
    private TableColumn<TopProduct, Integer> colQty;
    private TableColumn<TopProduct, Double> colRevenue;
    private LoadingOverlay loadingOverlay;

    private final ReportDao dao = new ReportDao();

    public Parent createView() {
        AuthGuard.requireLogin();

        BorderPane root = new BorderPane();
        root.setPrefSize(1100, 750);
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center content with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(30));

        // Stats cards row
        HBox statsRow = createStatsCards();

        // Charts row - Line chart and Pie chart side by side
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER);
        VBox lineChartCard = createLineChartCard();
        VBox pieChartCard = createPieChartCard();
        chartsRow.getChildren().addAll(lineChartCard, pieChartCard);

        // Bar chart card (existing)
        VBox barChartCard = createBarChartCard();

        // Top products card
        VBox productsCard = createProductsCard();

        // Action buttons
        HBox actionBar = createActionBar();

        centerBox.getChildren().addAll(statsRow, chartsRow, barChartCard, productsCard, actionBar);
        scrollPane.setContent(centerBox);
        root.setCenter(scrollPane);

        // Wrap in StackPane for loading overlay
        StackPane wrapper = new StackPane(root);

        // Add loading overlay
        loadingOverlay = new LoadingOverlay();
        wrapper.getChildren().add(loadingOverlay.getOverlay());

        // Load data with loading spinner
        loadingOverlay.show("Loading analytics data...");
        javafx.application.Platform.runLater(() -> {
            loadData();
            loadingOverlay.hide();
        });

        return wrapper;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label iconLabel = new Label("📊");
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label titleLabel = new Label("Sales Dashboard");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = createHeaderButton("← Back to Orders");
        backBtn.setOnAction(e -> onBack());

        Button logoutBtn = createHeaderButton("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;");
        logoutBtn.setOnMouseEntered(ev -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.5); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnMouseExited(ev -> logoutBtn.setStyle("-fx-background-color: rgba(220,53,69,0.3); -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600;"));
        logoutBtn.setOnAction(e -> onLogout());

        topBar.getChildren().addAll(iconLabel, titleLabel, spacer, backBtn, logoutBtn);
        return topBar;
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

    private HBox createStatsCards() {
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);

        // Total Revenue Card
        VBox revenueCard = createStatCard("💰", "Total Revenue", "$0.00", "#667eea", "revenue");

        // Total Orders Card
        VBox ordersCard = createStatCard("📦", "Total Orders", "0", "#28a745", "orders");

        // Total Customers Card
        VBox customersCard = createStatCard("👥", "Total Customers", "0", "#ffc107", "customers");

        statsRow.getChildren().addAll(revenueCard, ordersCard, customersCard);
        return statsRow;
    }

    private VBox createStatCard(String icon, String title, String value, String color, String type) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        // Create and store reference to value label based on type
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        // Store references
        switch (type) {
            case "revenue": totalRevenueLabel = valueLabel; break;
            case "orders": totalOrdersLabel = valueLabel; break;
            case "customers": totalCustomersLabel = valueLabel; break;
        }

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    private VBox createBarChartCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(1000);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📈");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Daily Sales Performance");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelRotation(45); // Rotate labels for better readability
        xAxis.setTickLabelGap(5);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue ($)");

        xAxis.lookup(".axis-label").setStyle("-fx-text-fill: #1abc9c;");
        yAxis.lookup(".axis-label").setStyle("-fx-text-fill: #1abc9c;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.DARKBLUE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.DARKGREEN);

        salesChart = new BarChart<>(xAxis, yAxis);
        salesChart.setPrefHeight(350);
        salesChart.setLegendVisible(false);
        salesChart.setStyle("-fx-background-color: transparent;");

        // Set chart title to show date range
        salesChart.setTitle("Sales by Date");

        card.getChildren().addAll(header, salesChart);
        return card;
    }

    private VBox createLineChartCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(480);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📊");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Revenue Trend");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelRotation(45);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue ($)");

        revenueLineChart = new LineChart<>(xAxis, yAxis);
        revenueLineChart.setPrefHeight(300);
        revenueLineChart.setLegendVisible(true);
        revenueLineChart.setStyle("-fx-background-color: transparent;");
        revenueLineChart.setCreateSymbols(true);

        card.getChildren().addAll(header, revenueLineChart);
        return card;
    }

    private VBox createPieChartCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(480);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🥧");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Sales by Category");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Chart
        categoryPieChart = new PieChart();
        categoryPieChart.setPrefHeight(300);
        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setStyle("-fx-background-color: transparent;");

        card.getChildren().addAll(header, categoryPieChart);
        return card;
    }

    private VBox createProductsCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(1000);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e1e4e8; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🏆");
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label("Top Selling Products");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Table
        topProductsTable = new TableView<>();
        topProductsTable.setPrefHeight(250);
        topProductsTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        colProduct = new TableColumn<>("Product Name");
        colProduct.setPrefWidth(400);
        colProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductName()));

        colQty = new TableColumn<>("Quantity Sold");
        colQty.setPrefWidth(200);
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantitySold()).asObject());

        colRevenue = new TableColumn<>("Revenue Generated");
        colRevenue.setPrefWidth(200);
        colRevenue.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getRevenue()).asObject());
        colRevenue.setCellFactory(col -> new TableCell<TopProduct, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                    setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                }
            }
        });

        topProductsTable.getColumns().addAll(colProduct, colQty, colRevenue);

        card.getChildren().addAll(header, topProductsTable);
        return card;
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER);

        Button refreshBtn = new Button("🔄 Refresh Data");
        refreshBtn.setPrefWidth(180);
        refreshBtn.setPrefHeight(45);
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10;");
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10;"));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10;"));
        refreshBtn.setOnAction(e -> onRefresh());

        Button exportBtn = new Button("💾 Export CSV");
        exportBtn.setPrefWidth(180);
        exportBtn.setPrefHeight(45);
        exportBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10;");
        exportBtn.setOnMouseEntered(e -> exportBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10;"));
        exportBtn.setOnMouseExited(e -> exportBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10;"));
        exportBtn.setOnAction(e -> onExportCSV());

        actionBar.getChildren().addAll(refreshBtn, exportBtn);
        return actionBar;
    }

    private void loadData() {
        // Total revenue
        double total = dao.getTotalRevenue();
        totalRevenueLabel.setText("$" + String.format("%.2f", total));

        // Total orders
        int totalOrders = dao.getTotalOrders();
        totalOrdersLabel.setText(String.valueOf(totalOrders));

        // Total customers
        int totalCustomers = dao.getTotalCustomers();
        totalCustomersLabel.setText(String.valueOf(totalCustomers));

        // Daily sales bar chart
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        dao.getDailySales().forEach(s -> series.getData().add(new XYChart.Data<>(s.getDate(), s.getRevenue())));
        series.setName("Daily Revenue");
        salesChart.getData().add(series);

        // Revenue line chart
        revenueLineChart.getData().clear();
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        dao.getDailySales().forEach(s -> lineSeries.getData().add(new XYChart.Data<>(s.getDate(), s.getRevenue())));
        lineSeries.setName("Revenue Trend");
        revenueLineChart.getData().add(lineSeries);

        // Category pie chart
        categoryPieChart.getData().clear();
        dao.getCategorySales().forEach(cs ->
            categoryPieChart.getData().add(new PieChart.Data(
                cs.getCategory() + " ($" + String.format("%.0f", cs.getRevenue()) + ")",
                cs.getRevenue()
            ))
        );

        // Top-selling products
        topProductsTable.setItems(FXCollections.observableArrayList(dao.getTopSellingProducts()));
    }

    private void onRefresh() {
        loadingOverlay.show("Refreshing data...");
        javafx.application.Platform.runLater(() -> {
            loadData();
            loadingOverlay.hide();
            ToastNotification.success("Data refreshed successfully!");
        });
    }

    private void onExportCSV() {
        try (FileWriter writer = new FileWriter("sales_report.csv")) {
            writer.write("Product,Quantity Sold,Revenue\n");
            for (TopProduct tp : dao.getTopSellingProducts()) {
                writer.write(String.format("%s,%d,%.2f\n", tp.getProductName(), tp.getQuantitySold(), tp.getRevenue()));
            }
            showStyledAlert("Export Successful", "✅ CSV exported to: sales_report.csv", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Export Failed", "❌ Error exporting CSV: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void onBack() {
        HelloApplication.setRoot(new AdminOrdersController());
    }

    private void onLogout() {
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