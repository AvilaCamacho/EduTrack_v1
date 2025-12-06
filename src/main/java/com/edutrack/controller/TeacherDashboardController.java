package com.edutrack.controller;

import com.edutrack.database.*;
import com.edutrack.model.*;
import com.edutrack.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class TeacherDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<Group> groupListView;

    @FXML
    private TableView<GroupStudent> studentsTable;

    @FXML
    private TableColumn<GroupStudent, String> studentNameColumn;

    @FXML
    private Button createGroupButton;

    @FXML
    private Button deleteGroupButton;

    @FXML
    private Button addStudentButton;

    @FXML
    private Button removeStudentButton;

    @FXML
    private Button takeAttendanceButton;

    private GroupDAO groupDAO;
    private GroupStudentDAO groupStudentDAO;
    private AttendanceDAO attendanceDAO;
    private UserDAO userDAO;
    private User currentUser;

    public TeacherDashboardController() {
        groupDAO = new GroupDAO();
        groupStudentDAO = new GroupStudentDAO();
        attendanceDAO = new AttendanceDAO();
        userDAO = new UserDAO();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Bienvenido, " + currentUser.getFullName());

        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        groupListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> loadStudentsForGroup(newValue)
        );

        loadGroups();
    }

    private void loadGroups() {
        List<Group> groups = groupDAO.getGroupsByTeacher(currentUser.getId());
        groupListView.getItems().setAll(groups);
    }

    private void loadStudentsForGroup(Group group) {
        if (group == null) {
            studentsTable.getItems().clear();
            return;
        }

        List<GroupStudent> students = groupStudentDAO.getStudentsByGroup(group.getId());
        studentsTable.getItems().setAll(students);
    }

    @FXML
    private void handleCreateGroup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear Grupo");
        dialog.setHeaderText("Crear un nuevo grupo");
        dialog.setContentText("Nombre del grupo:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                TextInputDialog descDialog = new TextInputDialog();
                descDialog.setTitle("Crear Grupo");
                descDialog.setHeaderText("Descripción del grupo");
                descDialog.setContentText("Descripción:");

                Optional<String> descResult = descDialog.showAndWait();
                String description = descResult.orElse("");

                Group newGroup = new Group();
                newGroup.setName(name.trim());
                newGroup.setDescription(description);
                newGroup.setTeacherId(currentUser.getId());

                if (groupDAO.createGroup(newGroup)) {
                    showInfo("Grupo creado exitosamente");
                    loadGroups();
                } else {
                    showError("Error al crear el grupo");
                }
            }
        });
    }

    @FXML
    private void handleDeleteGroup() {
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Grupo");
        alert.setHeaderText("¿Está seguro de eliminar el grupo?");
        alert.setContentText(selectedGroup.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (groupDAO.deleteGroup(selectedGroup.getId())) {
                showInfo("Grupo eliminado exitosamente");
                loadGroups();
                studentsTable.getItems().clear();
            } else {
                showError("Error al eliminar el grupo");
            }
        }
    }

    @FXML
    private void handleAddStudent() {
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            return;
        }

        List<User> allStudents = userDAO.getAllStudents();
        ChoiceDialog<User> dialog = new ChoiceDialog<>(null, allStudents);
        dialog.setTitle("Agregar Alumno");
        dialog.setHeaderText("Seleccione un alumno para agregar al grupo");
        dialog.setContentText("Alumno:");

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(student -> {
            if (groupStudentDAO.addStudentToGroup(selectedGroup.getId(), student.getId())) {
                showInfo("Alumno agregado exitosamente");
                loadStudentsForGroup(selectedGroup);
            } else {
                showError("Error al agregar alumno (puede que ya esté en el grupo)");
            }
        });
    }

    @FXML
    private void handleRemoveStudent() {
        GroupStudent selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showError("Por favor seleccione un alumno");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Alumno");
        alert.setHeaderText("¿Está seguro de eliminar al alumno del grupo?");
        alert.setContentText(selectedStudent.getStudentName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (groupStudentDAO.removeStudentFromGroup(selectedStudent.getId())) {
                showInfo("Alumno eliminado del grupo");
                Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
                loadStudentsForGroup(selectedGroup);
            } else {
                showError("Error al eliminar alumno");
            }
        }
    }

    @FXML
    private void handleTakeAttendance() {
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            return;
        }

        List<GroupStudent> students = groupStudentDAO.getStudentsByGroup(selectedGroup.getId());
        if (students.isEmpty()) {
            showError("No hay alumnos en este grupo");
            return;
        }

        // Create attendance dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Pasar Lista");
        dialog.setHeaderText("Grupo: " + selectedGroup.getName());

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ListView<CheckBox> attendanceList = new ListView<>();
        students.forEach(student -> {
            CheckBox checkBox = new CheckBox(student.getStudentName());
            checkBox.setSelected(true); // Default to present
            checkBox.setUserData(student.getStudentId());
            attendanceList.getItems().add(checkBox);
        });

        dialog.getDialogPane().setContent(attendanceList);

        Optional<Void> result = dialog.showAndWait();
        if (result.isPresent()) {
            attendanceList.getItems().forEach(checkBox -> {
                int studentId = (int) checkBox.getUserData();
                boolean present = checkBox.isSelected();
                attendanceDAO.recordAttendance(selectedGroup.getId(), studentId, present);
            });
            showInfo("Asistencia registrada exitosamente");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
