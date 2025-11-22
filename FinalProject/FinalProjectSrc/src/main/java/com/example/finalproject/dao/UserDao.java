package com.example.finalproject.dao;

import com.example.finalproject.model.User;
import java.sql.*;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByEmail(String email) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void save(User user) {
        try (Connection conn = DBConnection.getInstance()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(name,email,password_hash,role,address) VALUES(?,?,?,?,?)");
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getAddress());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean emailExists(String email) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updatePassword(String email, String newPassword) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash=? WHERE email=?")) {
            String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
            ps.setString(1, hashed);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("address")
        );
    }

    public String getEmailById(int userId) {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
