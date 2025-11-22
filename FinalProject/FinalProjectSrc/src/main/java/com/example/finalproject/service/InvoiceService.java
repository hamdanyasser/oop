package com.example.finalproject.service;

import com.example.finalproject.dao.OrderDao;
import com.example.finalproject.model.Order;
import com.example.finalproject.model.OrderItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class InvoiceService {

    private final OrderDao orderDao = new OrderDao();

    public Path generateInvoice(int orderId) throws Exception {
        // 🔹 Get order and its items
        Order order = orderDao.findById(orderId);
        List<OrderItem> items = orderDao.findItemsByOrder(orderId);

        if (order == null)
            throw new SQLException("Order not found for ID " + orderId);

        // 🔹 File path
        Path pdfPath = Path.of(System.getProperty("user.dir"), "invoice_" + orderId + ".pdf");

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath.toFile()));
        document.open();

        // 🔹 Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
        Paragraph title = new Paragraph("Invoice for Order #" + orderId, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // 🔹 Customer and Order Info
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        document.add(new Paragraph("Customer ID: " + order.getUserId(), infoFont));
        document.add(new Paragraph("Status: " + order.getStatus(), infoFont));
        document.add(new Paragraph("Created At: " + order.getCreatedAt(), infoFont));
        document.add(Chunk.NEWLINE);

        // 🔹 Items Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{4, 2, 2, 2});

        addTableHeader(table, "Product ID", "Quantity", "Price ($)", "Subtotal ($)");

        double total = 0;
        for (OrderItem item : items) {
            table.addCell(String.valueOf(item.getProductId()));
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("%.2f", item.getPrice()));
            double sub = item.getQuantity() * item.getPrice();
            table.addCell(String.format("%.2f", sub));
            total += sub;
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // 🔹 Total
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph totalP = new Paragraph("Total: $" + String.format("%.2f", total), totalFont);
        totalP.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalP);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Thank you for your purchase!", infoFont));

        document.close();
        return pdfPath;
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
}
