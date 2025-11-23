package com.example.finalproject.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static Properties dbProperties;

    private DBConnection() {}

    




    private static Properties loadDatabaseConfig() throws SQLException {
        if (dbProperties == null) {
            dbProperties = new Properties();
            try (InputStream input = DBConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {

                if (input == null) {
                    throw new SQLException(
                        "❌ Unable to find db.properties file. " +
                        "Please copy db.properties.example to db.properties and configure your database credentials."
                    );
                }

                dbProperties.load(input);

                
                if (!dbProperties.containsKey("db.url") ||
                    !dbProperties.containsKey("db.username") ||
                    !dbProperties.containsKey("db.password")) {
                    throw new SQLException(
                        "❌ db.properties is missing required fields (db.url, db.username, db.password)"
                    );
                }

            } catch (IOException e) {
                throw new SQLException("❌ Error loading database configuration: " + e.getMessage(), e);
            }
        }
        return dbProperties;
    }

    




    public static Connection getInstance() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Properties config = loadDatabaseConfig();
            String url = config.getProperty("db.url");
            String username = config.getProperty("db.username");
            String password = config.getProperty("db.password");

            return DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException e) {
            throw new SQLException("❌ MySQL Driver not found!", e);
        }
    }
}
