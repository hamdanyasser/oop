package com.example.finalproject.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection instance;

    private DBConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/pr1",
                        "root",
                        "Rony2003ae"
                );
            } catch (ClassNotFoundException e) {
                throw new SQLException("❌ MySQL Driver not found!", e);
            }
        }
        return instance;
    }
}
