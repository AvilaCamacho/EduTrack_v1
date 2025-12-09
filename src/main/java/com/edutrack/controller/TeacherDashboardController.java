package com.edutrack.controller;

import com.edutrack.database.*;
import com.edutrack.model.*;
import com.edutrack.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ProgressIndicator;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class TeacherDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<Group> groupListView;

    @FXML
    private BorderPane mainPane;

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

    @FXML
    private Button menuDashboardButton;

    @FXML
    private Button menuGroupsButton;

    @FXML
    private Button menuReportsButton;

    @FXML
    private Button menuSettingsButton;

    @FXML
    private ImageView logoImage;

    @FXML
    private VBox centerPlaceholder;

    @FXML
    private javafx.scene.control.TextField searchField;

    @FXML
    private VBox rightRail;

    // internal list copy for searching
    private final ObservableList<Group> allGroups = FXCollections.observableArrayList();

    // Modal overlay and confirm button referenced from FXML
    @FXML
    private Pane modalOverlay;

    @FXML
    private Button confirmDeleteButton;

    @FXML
    private Label modalTitleLabel;

    @FXML
    private Label modalBodyLabel;

    private final GroupDAO groupDAO;
    private final GroupStudentDAO groupStudentDAO;
    private final AttendanceDAO attendanceDAO;
    private final UserDAO userDAO;
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
        String name = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Usuario";
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenido, " + name);
        }

        // Load logo image (use login1.jpg as default)
        try (InputStream is = getClass().getResourceAsStream("/images/login1.jpg")) {
            if (is != null && logoImage != null) {
                Image img = new Image(is);
                logoImage.setImage(img);
                logoImage.setPreserveRatio(true);
                logoImage.setFitHeight(40);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar logo: " + e.getMessage());
        }

        // ListView cell factory
        if (groupListView != null) {
            groupListView.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else {
                        setText(item.getName());
                    }
                }
            });

            groupListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (studentsTable != null) {
                        loadStudentsForGroup(newValue);
                    }
                }
            );

            groupListView.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) {
                    Group g = groupListView.getSelectionModel().getSelectedItem();
                    if (g != null) openGroupsView(g.getId());
                }
            });
        }

        // cargar grupos
        loadGroups();

        // search listener for filtering
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> {
                filterGroups(newV);
            });
        }

        // show dashboard by default (load in background)
        Platform.runLater(this::handleShowDashboard);

        // Ensure modal is hidden at start
        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    private void filterGroups(String q) {
        if (q == null || q.trim().isEmpty()) {
            groupListView.getItems().setAll(allGroups);
            return;
        }
        String lower = q.toLowerCase();
        ObservableList<Group> filtered = FXCollections.observableArrayList();
        for (Group g : allGroups) {
            if (g.getName() != null && g.getName().toLowerCase().contains(lower)) {
                filtered.add(g);
            }
        }
        groupListView.getItems().setAll(filtered);
    }

    private void loadGroups() {
        if (groupListView == null) return;
        int teacherId = (currentUser != null) ? currentUser.getId() : 1;
        List<Group> groups = groupDAO.getGroupsByTeacher(teacherId);
        allGroups.clear();
        if (groups != null && !groups.isEmpty()) allGroups.addAll(groups);
        groupListView.getItems().setAll(allGroups);
    }

    private void loadStudentsForGroup(Group group) {
        if (studentsTable == null) return; // tabla no presente en esta vista
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

        // Show modal overlay with confirmation
        if (modalTitleLabel != null) modalTitleLabel.setText("Eliminar grupo: " + selectedGroup.getName());
        if (modalBodyLabel != null) modalBodyLabel.setText("¿Desea eliminar el grupo '" + selectedGroup.getName() + "'? Esta acción es irreversible.");
        if (modalOverlay != null) {
            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);
        }
        // confirmDeleteButton will call handleConfirmDelete
    }

    @FXML
    private void handleAddStudent() {
        // Este handler sólo funciona desde la vista Grupos completa (no desde el Dashboard simplificado)
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            return;
        }

        if (studentsTable == null) {
            showError("Abra la vista 'Grupos' para gestionar alumnos de un grupo");
            return;
        }

        List<User> allStudents = userDAO.getAllStudents();
        // Construir un diálogo personalizado con ComboBox para mostrar nombres correctamente
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Agregar Alumno");
        dialog.setHeaderText("Seleccione un alumno para agregar al grupo");

        ButtonType addBtn = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        javafx.scene.control.ComboBox<User> combo = new javafx.scene.control.ComboBox<>(FXCollections.observableArrayList(allStudents));
        combo.setPrefWidth(360);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getFullName() != null && !item.getFullName().trim().isEmpty() ? item.getFullName() : item.getUsername());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getFullName() != null && !item.getFullName().trim().isEmpty() ? item.getFullName() : item.getUsername());
            }
        });
        if (!allStudents.isEmpty()) combo.getSelectionModel().selectFirst();

        dialog.getDialogPane().setContent(combo);

        dialog.setResultConverter(bt -> bt == addBtn ? combo.getSelectionModel().getSelectedItem() : null);

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
        if (studentsTable == null) {
            showError("Abra la vista 'Grupos' para gestionar alumnos de un grupo");
            return;
        }

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

    @FXML
    private void handleConfirmDelete() {
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            if (modalOverlay != null) {
                modalOverlay.setVisible(false);
                modalOverlay.setManaged(false);
            }
            return;
        }

        if (groupDAO.deleteGroup(selectedGroup.getId())) {
            showInfo("Grupo eliminado exitosamente");
            loadGroups();
            studentsTable.getItems().clear();
        } else {
            showError("Error al eliminar el grupo");
        }

        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    @FXML
    private void handleCancelDelete() {
        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    @FXML
    private void handleShowDashboard() {
        if (centerPlaceholder == null) return;
        setActiveMenu(menuDashboardButton);
        centerPlaceholder.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            javafx.scene.Parent node = loader.load();
            centerPlaceholder.getChildren().add(node);
        } catch (Exception e) {
            // fallback: create simple metric cards
            javafx.scene.layout.HBox metrics = new javafx.scene.layout.HBox(12);
            metrics.getStyleClass().add("metrics-row");

            VBox card1 = createMetricCard("Estudiantes Presentes", "28", "present");
            VBox card2 = createMetricCard("Estudiantes Ausentes", "4", "absent");
            VBox card3 = createMetricCard("Retardos", "2", "late");

            metrics.getChildren().addAll(card1, card2, card3);

            centerPlaceholder.getChildren().addAll(new Label("\n"), metrics);
        }
    }

    @FXML
    private void handleShowGroups() {
        openGroupsView(null);
    }

    private void openGroupsView(Integer groupId) {
        if (centerPlaceholder == null) return;
        setActiveMenu(menuGroupsButton);
        centerPlaceholder.getChildren().clear();

        // Create overlay with spinner
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("loading-overlay");
        overlay.setPrefSize(centerPlaceholder.getWidth(), centerPlaceholder.getHeight());
        ProgressIndicator pi = new ProgressIndicator();
        overlay.getChildren().add(pi);
        centerPlaceholder.getChildren().add(overlay);

        // Load the FXML on the JavaFX Application Thread to avoid thread issues
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GroupsList.fxml"));
                Parent node = loader.load();
                com.edutrack.controller.GroupsController controller = loader.getController();
                if (groupId != null) controller.selectGroupById(groupId);
                centerPlaceholder.getChildren().clear();
                centerPlaceholder.getChildren().add(node);
            } catch (Exception e) {
                centerPlaceholder.getChildren().clear();
                showError("Error al abrir vista de grupos: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleShowReports() {
        if (centerPlaceholder == null) return;
        setActiveMenu(menuReportsButton);
        centerPlaceholder.getChildren().clear();
        Label title = new Label("Reportes (próximamente)");
        title.getStyleClass().add("section-title");
        centerPlaceholder.getChildren().add(title);
    }

    @FXML
    private void handleShowSettings() {
        if (centerPlaceholder == null) return;
        setActiveMenu(menuSettingsButton);
        centerPlaceholder.getChildren().clear();
        Label title = new Label("Ajustes");
        title.getStyleClass().add("section-title");
        centerPlaceholder.getChildren().add(title);
    }

    private void setActiveMenu(Button active) {
        Button[] all = { menuDashboardButton, menuGroupsButton, menuReportsButton, menuSettingsButton };
        for (Button b : all) {
            if (b == null) continue;
            b.getStyleClass().remove("menu-button-active");
        }
        if (active != null) {
            if (!active.getStyleClass().contains("menu-button-active")) {
                active.getStyleClass().add("menu-button-active");
            }
        }
    }

    private VBox createMetricCard(String label, String value, String style) {
        VBox v = new VBox(6);
        v.getStyleClass().addAll("card", "metric-card");
        Label l = new Label(label);
        l.getStyleClass().add("metric-label");
        Label vL = new Label(value);
        vL.getStyleClass().addAll("metric-value", style);
        v.getChildren().addAll(l, vL);
        v.setPrefWidth(220);
        return v;
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