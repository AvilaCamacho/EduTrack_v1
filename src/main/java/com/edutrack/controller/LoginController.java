package com.edutrack.controller;

import com.edutrack.database.UserDAO;
import com.edutrack.model.User;
import com.edutrack.util.SessionManager;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    private UserDAO userDAO;

    public LoginController() {
        userDAO = new UserDAO();
    }

    @FXML
    private void initialize() {
        messageLabel.setText("");
        loginButton.setDisable(true);

        // Enable login button only when both fields have text
        usernameField.textProperty().addListener(this::onInputChanged);
        passwordField.textProperty().addListener(this::onInputChanged);

        // allow pressing Enter in password field to submit
        passwordField.setOnAction(e -> {
            if (!loginButton.isDisable()) {
                handleLogin();
            }
        });
    }

    private void onInputChanged(ObservableValue<? extends String> obs, String oldVal, String newVal) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        loginButton.setDisable(username.isEmpty() || password.isEmpty());
        // Clear message when user types
        if (!messageLabel.getText().isEmpty()) {
            messageLabel.setText("");
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

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            showSuccess("Inicio de sesión exitoso");
            openDashboard(user);
        } else {
            showError("Usuario o contraseña incorrectos");
        }
    }

    private void openDashboard(User user) {
        try {
            String fxmlFile = user.getUserType() == User.UserType.TEACHER
                ? "/fxml/TeacherDashboard.fxml"
                : "/fxml/StudentDashboard.fxml";

            java.net.URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                showError("Archivo FXML no encontrado: " + fxmlFile);
                System.err.println("Resource not found for: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            java.net.URL css = getClass().getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            else System.err.println("CSS not found: /css/style.css");

            stage.setScene(scene);
            stage.setTitle("EduTrack - " + (user.getUserType() == User.UserType.TEACHER ? "Maestro" : "Alumno"));
            stage.setWidth(1100); // ampliar ventana
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar el panel de control: " + e.getMessage());
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #b23b3b; -fx-font-weight: 600;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #22577a; -fx-font-weight: 600;");
    }
}