package com.edutrack.controller;

import com.edutrack.database.UserDAO;
import com.edutrack.model.User;
import com.edutrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView heroImage;

    private final UserDAO userDAO;

    public LoginController() {
        userDAO = new UserDAO();
    }

    @FXML
    private void initialize() {
        messageLabel.setText("");

        // Load hero image safely from resources
        try (InputStream is = getClass().getResourceAsStream("/images/login1.jpg")) {
            if (is != null && heroImage != null) {
                Image img = new Image(is);
                heroImage.setImage(img);
            }
        } catch (Exception e) {
            // ignore image load failures but log to console
            System.err.println("No se pudo cargar la imagen de hero: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese usuario y contraseña");
            return;
        }

        // Prevent double clicks while loading
        loginButton.setDisable(true);

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            openDashboard(user);
        } else {
            showError("Usuario o contraseña incorrectos");
            loginButton.setDisable(false);
        }
    }

    private void openDashboard(User user) {
        try {
            String fxmlResource = user.getUserType() == User.UserType.TEACHER
                ? "fxml/TeacherDashboard.fxml"
                : "fxml/StudentDashboard.fxml";

            // Prefer classpath resource via getResource using leading slash
            URL fxmlUrl = getClass().getResource("/" + fxmlResource);
            if (fxmlUrl == null) {
                // fallback to context class loader
                fxmlUrl = Thread.currentThread().getContextClassLoader().getResource(fxmlResource);
            }

            if (fxmlUrl == null) {
                throw new IOException("No se encontró el recurso FXML: " + fxmlResource);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);

            // Load CSS safely
            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Set window properties: maximized but not fullscreen to keep taskbar visible
            stage.setScene(scene);
            stage.setTitle("EduTrack - " + (user.getUserType() == User.UserType.TEACHER ? "Maestro" : "Alumno"));
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            // Log stacktrace to console for debugging
            System.err.println("Error al cargar dashboard: " + e.getMessage());
            e.printStackTrace(System.err);

            // Show more detailed dialog so the user knows the cause
            showErrorWithDetails("Error al cargar el panel de control", e);
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");

        // Also show a small alert for visibility
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorWithDetails(String message, Exception e) {
        // Set label
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");

        // Show detailed alert with expandable stack trace
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        String content = e.getClass().getSimpleName() + ": " + e.getMessage();
        alert.setContentText(content);

        // Create expandable Exception area
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setExpandableContent(textArea);

        alert.showAndWait();
    }
}