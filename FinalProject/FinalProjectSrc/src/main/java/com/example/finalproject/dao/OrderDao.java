package com.example.finalproject.dao;

import com.example.finalproject.model.Order;
import com.example.finalproject.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public void saveOrder(Order order) throws SQLException {
        Connection conn = DBConnection.getInstance();

        conn.setAutoCommit(false);
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO orders(user_id,total,status) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setDouble(2, order.getTotal());
            ps.setString(3, order.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) order.setId(rs.getInt(1));

            PreparedStatement itemStmt = conn.prepareStatement(
                    "INSERT INTO order_items(order_id,product_id,quantity,price) VALUES(?,?,?,?)");
            for (OrderItem item : order.getItems()) {
                itemStmt.setInt(1, order.getId());
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM orders ORDER BY created_at DESC")) {
            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> filterOrders(String keyword, String status) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1 ");

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND status=? ");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (CAST(id AS CHAR) LIKE ? OR CAST(user_id AS CHAR) LIKE ?) ");
        }
        sql.append("ORDER BY created_at DESC");

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (status != null && !"ALL".equalsIgnoreCase(status)) {
                ps.setString(idx++, status);
            }
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateStatus(int id, String status) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Order findById(int id) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OrderItem> findItemsByOrder(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM order_items WHERE order_id=?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new OrderItem(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ✅ FIXED: When deleting, restore product stock
    public void delete(int id) {
        try (Connection conn = DBConnection.getInstance()) {
            conn.setAutoCommit(false);

            // Get order items before deleting
            List<OrderItem> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT product_id, quantity FROM order_items WHERE order_id=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    items.add(new OrderItem(0, id, rs.getInt("product_id"), rs.getInt("quantity"), 0));
                }
            }

            // ✅ Restore stock for each product
            try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET stock = stock + ? WHERE id = ?")) {
                for (OrderItem item : items) {
                    ps.setInt(1, item.getQuantity());
                    ps.setInt(2, item.getProductId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Delete order items first
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM order_items WHERE order_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // Delete order
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
