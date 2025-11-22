package com.example.finalproject.dao;

import com.example.finalproject.model.Product;
import com.example.finalproject.model.Platform;
import com.example.finalproject.model.Genre;
import java.sql.*;
import java.util.*;

public class ProductDao {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();

        String sql = """
        SELECT p.*,
               COALESCE(pr.discount, 0) AS discount
        FROM products p
        LEFT JOIN promotions pr
        ON (pr.product_id = p.id OR pr.category = p.category)
        AND CURDATE() BETWEEN pr.start_date AND pr.end_date
        ORDER BY p.id DESC
        """;

        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getInt("stock")
                );
                p.setDiscount(rs.getDouble("discount"));
                p.setAgeRating(rs.getString("age_rating"));
                p.setProductType(rs.getString("product_type"));
                loadPlatformsForProduct(p, conn);
                loadGenresForProduct(p, conn);
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get products filtered by platform
     * @param platformId The platform ID to filter by (null for all products)
     * @return List of products available on the specified platform
     */
    public List<Product> getProductsByPlatform(Integer platformId) {
        if (platformId == null) {
            return getAllProducts();
        }

        List<Product> list = new ArrayList<>();

        String sql = """
        SELECT DISTINCT p.*,
               COALESCE(pr.discount, 0) AS discount
        FROM products p
        INNER JOIN product_platforms pp ON p.id = pp.product_id
        LEFT JOIN promotions pr
        ON (pr.product_id = p.id OR pr.category = p.category)
        AND CURDATE() BETWEEN pr.start_date AND pr.end_date
        WHERE pp.platform_id = ?
        ORDER BY p.id DESC
        """;

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, platformId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getInt("stock")
                );
                p.setDiscount(rs.getDouble("discount"));
                p.setAgeRating(rs.getString("age_rating"));
                p.setProductType(rs.getString("product_type"));
                loadPlatformsForProduct(p, conn);
                loadGenresForProduct(p, conn);
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get products filtered by genre
     * @param genreId The genre ID to filter by (null for all products)
     * @return List of products in the specified genre
     */
    public List<Product> getProductsByGenre(Integer genreId) {
        if (genreId == null) {
            return getAllProducts();
        }

        List<Product> list = new ArrayList<>();

        String sql = """
        SELECT DISTINCT p.*,
               COALESCE(pr.discount, 0) AS discount
        FROM products p
        INNER JOIN product_genres pg ON p.id = pg.product_id
        LEFT JOIN promotions pr
        ON (pr.product_id = p.id OR pr.category = p.category)
        AND CURDATE() BETWEEN pr.start_date AND pr.end_date
        WHERE pg.genre_id = ?
        ORDER BY p.id DESC
        """;

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, genreId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("imagePath"),
                        rs.getInt("stock")
                );
                p.setDiscount(rs.getDouble("discount"));
                p.setAgeRating(rs.getString("age_rating"));
                p.setProductType(rs.getString("product_type"));
                loadPlatformsForProduct(p, conn);
                loadGenresForProduct(p, conn);
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Load platforms for a product
     */
    private void loadPlatformsForProduct(Product product, Connection conn) {
        String sql = """
            SELECT pl.id, pl.name
            FROM platforms pl
            INNER JOIN product_platforms pp ON pl.id = pp.platform_id
            WHERE pp.product_id = ?
            ORDER BY pl.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                product.addPlatform(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all available platforms
     */
    public List<Platform> getAllPlatforms() {
        List<Platform> platforms = new ArrayList<>();
        String sql = "SELECT * FROM platforms ORDER BY type, name";

        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Platform platform = new Platform(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getString("manufacturer"),
                    rs.getString("icon_path")
                );
                platforms.add(platform);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return platforms;
    }

    /**
     * Load genres for a product
     */
    private void loadGenresForProduct(Product product, Connection conn) {
        String sql = """
            SELECT g.id, g.name
            FROM genres g
            INNER JOIN product_genres pg ON g.id = pg.genre_id
            WHERE pg.product_id = ?
            ORDER BY g.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                product.addGenre(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all available genres
     */
    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT * FROM genres ORDER BY name";

        try (Connection conn = DBConnection.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Genre genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("icon_path")
                );
                genres.add(genre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return genres;
    }


    public Optional<Product> getById(int id) {
        String sql = """
            SELECT p.*,
                   COALESCE(pr.discount, 0) AS discount
            FROM products p
            LEFT JOIN promotions pr
            ON (pr.product_id = p.id OR pr.category = p.category)
            AND CURDATE() BETWEEN pr.start_date AND pr.end_date
            WHERE p.id = ?
            """;
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = map(rs);
                loadPlatformsForProduct(p, conn);
                loadGenresForProduct(p, conn);
                return Optional.of(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void insert(Product p) {
        String sql = "INSERT INTO products(name,category,price,description,imagePath,stock,age_rating,product_type) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getDescription());
            ps.setString(5, p.getImagePath());
            ps.setInt(6, p.getStock());
            ps.setString(7, p.getAgeRating());
            ps.setString(8, p.getProductType() != null ? p.getProductType() : "Physical");
            ps.executeUpdate();

            // Get generated product ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int productId = rs.getInt(1);
                p.setId(productId);

                // Save platform associations
                if (p.getPlatformIds() != null && !p.getPlatformIds().isEmpty()) {
                    savePlatformAssociations(productId, p.getPlatformIds(), conn);
                }

                // Save genre associations
                if (p.getGenreIds() != null && !p.getGenreIds().isEmpty()) {
                    saveGenreAssociations(productId, p.getGenreIds(), conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Product p) {
        String sql = """
                UPDATE products
                SET name=?, category=?, price=?, description=?, imagePath=?, stock=?, age_rating=?, product_type=?
                WHERE id=?
                """;
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getDescription());
            ps.setString(5, p.getImagePath());
            ps.setInt(6, p.getStock());
            ps.setString(7, p.getAgeRating());
            ps.setString(8, p.getProductType() != null ? p.getProductType() : "Physical");
            ps.setInt(9, p.getId());
            ps.executeUpdate();

            // Update platform associations
            deletePlatformAssociations(p.getId(), conn);
            if (p.getPlatformIds() != null && !p.getPlatformIds().isEmpty()) {
                savePlatformAssociations(p.getId(), p.getPlatformIds(), conn);
            }

            // Update genre associations
            deleteGenreAssociations(p.getId(), conn);
            if (p.getGenreIds() != null && !p.getGenreIds().isEmpty()) {
                saveGenreAssociations(p.getId(), p.getGenreIds(), conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decreaseStock(int productId, int quantity) {
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ?")) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save platform associations for a product
     */
    private void savePlatformAssociations(int productId, List<Integer> platformIds, Connection conn) throws SQLException {
        String sql = "INSERT INTO product_platforms (product_id, platform_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer platformId : platformIds) {
                ps.setInt(1, productId);
                ps.setInt(2, platformId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Delete all platform associations for a product
     */
    private void deletePlatformAssociations(int productId, Connection conn) throws SQLException {
        String sql = "DELETE FROM product_platforms WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    /**
     * Save genre associations for a product
     */
    private void saveGenreAssociations(int productId, List<Integer> genreIds, Connection conn) throws SQLException {
        String sql = "INSERT INTO product_genres (product_id, genre_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer genreId : genreIds) {
                ps.setInt(1, productId);
                ps.setInt(2, genreId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Delete all genre associations for a product
     */
    private void deleteGenreAssociations(int productId, Connection conn) throws SQLException {
        String sql = "DELETE FROM product_genres WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getString("description"),
                rs.getString("imagePath"),
                rs.getInt("stock")
        );
        p.setDiscount(rs.getDouble("discount"));
        p.setAgeRating(rs.getString("age_rating"));
        p.setProductType(rs.getString("product_type"));
        return p;
    }
}
