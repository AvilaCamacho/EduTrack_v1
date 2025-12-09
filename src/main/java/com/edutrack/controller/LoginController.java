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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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

    private final UserDAO userDAO;

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
        String fxmlFile = user.getUserType() == User.UserType.TEACHER
                ? "/fxml/TeacherDashboard.fxml"
                : "/fxml/StudentDashboard.fxml";

        // Preferir cargar por URL (classpath) para que las rutas relativas en FXML se resuelvan correctamente
        URL resourceUrl = getClass().getResource(fxmlFile);
        Parent root = null;
        try {
            if (resourceUrl != null) {
                FXMLLoader loader = new FXMLLoader(resourceUrl);
                root = loader.load();
            } else {
                // Intentar desde filesystem (IDE)
                java.io.File file = new java.io.File("src/main/resources" + fxmlFile);
                if (file.exists()) {
                    URL fileUrl = file.toURI().toURL();
                    System.out.println("Found FXML on filesystem at: " + file.getAbsolutePath());
                    FXMLLoader loader = new FXMLLoader(fileUrl);
                    root = loader.load();
                } else {
                    showError("Archivo FXML no encontrado: " + fxmlFile);
                    openFallbackDashboard();
                    return;
                }
            }
        } catch (Throwable t) {
            // fallo al cargar FXML -> intentar fallback alternativo y mostrar traza completa
            t.printStackTrace();
            showError("Error al cargar el panel de control. Se abrirá una vista de emergencia.");

            // mostrar un Alert con la stacktrace para debugging
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String exceptionText = sw.toString();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al cargar FXML");
            alert.setHeaderText("Ocurrió un error al cargar: " + fxmlFile);
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();

            openFallbackDashboard();
            return;
        }

        try {
            if (root == null) {
                showError("No se pudo preparar la vista para mostrar");
                return;
            }
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            java.net.URL css = getClass().getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            else System.err.println("CSS not found: /css/style.css");

            stage.setScene(scene);
            stage.setTitle("EduTrack - " + (user.getUserType() == User.UserType.TEACHER ? "Maestro" : "Alumno"));
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al mostrar el panel de control: " + e.getMessage());
        }
    }

    private void openFallbackDashboard() {
        // Construir una UI mínima y estética para que el usuario pueda continuar
        BorderPane root = new BorderPane();
        VBox center = new VBox(12);
        Text title = new Text("EduTrack - Demo Dashboard");
        title.setStyle("-fx-font-size:24px; -fx-fill: linear-gradient(#CFEFFF, #9FDFFF);");
        Text info = new Text("No se pudo cargar la vista completa. Esta es una versión de emergencia.");
        info.setStyle("-fx-fill: #9fbfd0;");
        center.getChildren().addAll(title, info);
        root.setCenter(center);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root, 1000, 700);
        java.net.URL css = getClass().getResource("/css/student-dashboard.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setTitle("EduTrack - Demo");
        stage.setMaximized(true);
        stage.show();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #c62b2b; -fx-font-weight: 700;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #0b66a3; -fx-font-weight: 700;");
    }
}