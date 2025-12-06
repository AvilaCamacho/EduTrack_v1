package com.edutrack.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Database configuration - these should be configured via properties file
    private static final String DB_URL = "jdbc:oracle:thin:@"; // Will be completed with tnsnames
    private String walletLocation;
    private String username;
    private String password;
    private String tnsAlias;

    private DatabaseConnection() {
        // Private constructor for singleton
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public void configure(String walletLocation, String username, String password, String tnsAlias) {
        this.walletLocation = walletLocation;
        this.username = username;
        this.password = password;
        this.tnsAlias = tnsAlias;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    private void connect() throws SQLException {
        try {
            // Set Oracle Wallet location
            System.setProperty("oracle.net.tns_admin", walletLocation);
            System.setProperty("oracle.net.wallet_location", walletLocation);

            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            
            // For Oracle Cloud with wallet
            props.setProperty("oracle.jdbc.fanEnabled", "false");
            
            // Connect using TNS alias from tnsnames.ora
            String url = DB_URL + tnsAlias;
            
            connection = DriverManager.getConnection(url, props);
            System.out.println("Database connection established successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
