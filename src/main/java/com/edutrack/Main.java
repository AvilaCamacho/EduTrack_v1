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

            // Intenta precargar los FXML principales para detectar errores tempranos
            preloadFxml("/fxml/StudentDashboard.fxml");
            preloadFxml("/fxml/TeacherDashboard.fxml");

            // Si se pasa la propiedad edutrack.debugOpenStudent=true, abrir directamente el StudentDashboard (debug)
            String debugOpen = System.getProperty("edutrack.debugOpenStudent");
            if ("true".equalsIgnoreCase(debugOpen)) {
                try {
                    System.out.println("Debug mode: opening StudentDashboard directly");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentDashboard.fxml"));
                    Parent rootDebug = loader.load();
                    Scene sceneDebug = new Scene(rootDebug);
                    if (getClass().getResource("/css/style.css") != null) sceneDebug.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                    primaryStage.setScene(sceneDebug);
                    primaryStage.setTitle("EduTrack - Debug Student");
                    primaryStage.setMaximized(true);
                    primaryStage.show();
                    return; // ya mostramos la ventana
                } catch (Throwable t) {
                    System.err.println("Failed to open StudentDashboard in debug mode: " + t.getMessage());
                    t.printStackTrace();
                    // continuar y cargar login como fallback
                }
            }

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            // Establecer tamaño inicial amplio y consistente con el diseño
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("EduTrack - Sistema de Gestión Educativa");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true); // permitir redimensionar
            primaryStage.setWidth(1280); // tamaño inicial para dashboard
            primaryStage.setHeight(800);
            // Establecer min size para evitar compresión
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(720);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }

    private void preloadFxml(String resourcePath) {
        try {
            java.net.URL res = getClass().getResource(resourcePath);
            if (res == null) {
                System.err.println("Preload: resource not found on classpath: " + resourcePath);
                // try filesystem fallback for IDE
                File f = new File("src/main/resources" + resourcePath);
                if (f.exists()) {
                    System.out.println("Preload: found on filesystem: " + f.getAbsolutePath());
                    res = f.toURI().toURL();
                } else {
                    System.err.println("Preload: resource also not found on filesystem: " + f.getAbsolutePath());
                    return;
                }
            }
            System.out.println("Preloading FXML: " + resourcePath + " from " + res.toExternalForm());
            FXMLLoader loader = new FXMLLoader(res);
            Parent p = loader.load();
            System.out.println("Preloaded OK: " + resourcePath);
        } catch (Throwable t) {
            System.err.println("Error preloading FXML: " + resourcePath + " -> " + t.getMessage());
            t.printStackTrace();
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
                File walletDir = null;
                try {
                    java.net.URL resourceUrl = getClass().getResource("/wallet");
                    if (resourceUrl != null) {
                        walletDir = new File(resourceUrl.getPath());
                    }
                } catch (Exception e) {
                    System.err.println("Wallet resource not found in classpath: " + e.getMessage());
                }

                if (walletDir == null || !walletDir.exists()) {
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
                System.err.println("database.properties file not found.");
                System.err.println("Please create database.properties in src/main/resources/ with database credentials.");
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