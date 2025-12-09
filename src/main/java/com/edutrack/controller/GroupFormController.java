package com.edutrack.controller;

import com.edutrack.database.GroupDAO;
import com.edutrack.model.Group;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GroupFormController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descField;

    @FXML
    private TextField tutorField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private GroupDAO groupDAO = new GroupDAO();
    private Group editingGroup = null;

    @FXML
    private void initialize() {

    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        // TODO: validar
        if (name.isEmpty()) {
            // simple validation
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "El nombre del grupo es obligatorio");
            a.showAndWait();
            return;
        }
        if (editingGroup == null) {
            Group g = new Group();
            g.setName(name);
            g.setDescription(desc);
            // set teacher from session
            com.edutrack.model.User user = com.edutrack.util.SessionManager.getInstance().getCurrentUser();
            g.setTeacherId(user != null ? user.getId() : 1);
            if (groupDAO.createGroup(g)) {
                // cerrar
                closeWindow();
            } else {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Error al crear grupo");
                a.showAndWait();
            }
        } else {
            editingGroup.setName(name);
            editingGroup.setDescription(desc);
            if (groupDAO.updateGroup(editingGroup)) {
                closeWindow();
            } else {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Error al actualizar grupo");
                a.showAndWait();
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage s = (Stage) saveButton.getScene().getWindow();
        s.close();
    }

    public void setEditingGroup(Group g) {
        this.editingGroup = g;
        if (g != null) {
            nameField.setText(g.getName());
            descField.setText(g.getDescription());
        }
    }
}