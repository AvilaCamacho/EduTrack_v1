package com.edutrack.controller;

import com.edutrack.database.UserDAO;
import com.edutrack.model.User;
import com.edutrack.util.SessionManager;
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("EduTrack - " + (user.getUserType() == User.UserType.TEACHER ? "Maestro" : "Alumno"));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar el panel de control");
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
    }
}
