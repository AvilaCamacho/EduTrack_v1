package com.edutrack;

import com.edutrack.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load database configuration
            loadDatabaseConfig();

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("EduTrack - Sistema de Gestión Educativa");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }

    private void loadDatabaseConfig() {
        try {
            // Load configuration from properties file
            InputStream input = getClass().getResourceAsStream("/database.properties");
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);

                String walletPath = prop.getProperty("db.wallet.path");
                String username = prop.getProperty("db.username");
                String password = prop.getProperty("db.password");
                String tnsAlias = prop.getProperty("db.tns.alias");

                // Get the absolute path to the wallet directory
                String resourcePath = getClass().getResource("/wallet").getPath();
                File walletDir = new File(resourcePath);
                
                if (!walletDir.exists()) {
                    // If wallet doesn't exist in resources, use the configured path
                    walletDir = new File(walletPath);
                }

                DatabaseConnection.getInstance().configure(
                    walletDir.getAbsolutePath(),
                    username,
                    password,
                    tnsAlias
                );

                System.out.println("Database configuration loaded successfully");
            } else {
                System.err.println("database.properties file not found. Using default configuration.");
                // Set default values or prompt user for configuration
            }
        } catch (Exception e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Clean up database connection when application closes
        DatabaseConnection.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
