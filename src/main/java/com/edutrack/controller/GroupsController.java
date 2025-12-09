package com.edutrack.controller;

import com.edutrack.database.GroupDAO;
import com.edutrack.database.GroupStudentDAO;
import com.edutrack.model.Group;
import com.edutrack.util.SessionManager;
import com.edutrack.model.User;
import com.edutrack.model.GroupStudent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class GroupsController {

    @FXML
    private TableView<Group> groupsTable;

    @FXML
    private ListView<Group> groupListView;

    @FXML
    private TableColumn<Group, Integer> colId;

    @FXML
    private TableColumn<Group, String> colName;

    @FXML
    private TableColumn<Group, Integer> colStudents;

    @FXML
    private TableColumn<Group, Node> colActions;

    @FXML
    private FlowPane studentsFlow;

    @FXML
    private Label selectedGroupLabel;

    @FXML
    private Button addStudentBtn;

    @FXML
    private Button deleteGroupButton;

    @FXML
    private Button takeAttendanceBtn;

    private final GroupDAO groupDAO = new GroupDAO();
    private final GroupStudentDAO groupStudentDAO = new GroupStudentDAO();

    private Group selectedGroup;

    @FXML
    private void initialize() {
        // If old TableView is present, keep table behavior
        if (groupsTable != null) {
            setupColumns();
            loadGroups();
            groupsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldG, newG) -> {
                selectedGroup = newG;
                if (newG != null) {
                    selectedGroupLabel.setText(newG.getName());
                    loadStudentsCards(newG.getId());
                } else {
                    selectedGroupLabel.setText("Selecciona un grupo");
                    studentsFlow.getChildren().clear();
                }
            });
        }

        // If new ListView is present, use it for faster group navigation
        if (groupListView != null) {
            groupListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            loadGroups();
            groupListView.getSelectionModel().selectedItemProperty().addListener((obs, oldG, newG) -> {
                selectedGroup = newG;
                if (newG != null) {
                    selectedGroupLabel.setText(newG.getName());
                    loadStudentsCards(newG.getId());
                } else {
                    selectedGroupLabel.setText("Selecciona un grupo");
                    studentsFlow.getChildren().clear();
                }
            });
        }

        // conectar el botón del FXML al handler (elimina la advertencia de campo no usado)
        if (addStudentBtn != null) {
            addStudentBtn.setOnAction(e -> handleAddStudentToGroup());
        }
    }

    private void setupColumns() {
        if (groupsTable == null) return; // nothing to setup when using ListView
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());
        colName.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getName()));
        colStudents.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(
                groupStudentDAO.getStudentsByGroup(cell.getValue().getId()).size()).asObject());

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button delBtn = new Button("Eliminar");
            {
                editBtn.setOnAction(e -> {
                    Group g = getTableView().getItems().get(getIndex());
                    openGroupForm(g);
                });
                delBtn.setOnAction(e -> {
                    Group g = getTableView().getItems().get(getIndex());
                    handleDelete(g);
                });
            }

            @Override
            protected void updateItem(Node item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AnchorPane wrapper = new AnchorPane();
                    editBtn.getStyleClass().add("secondary-button");
                    delBtn.getStyleClass().add("danger-button");
                    wrapper.getChildren().addAll(editBtn, delBtn);
                    AnchorPane.setLeftAnchor(editBtn, 0.0);
                    AnchorPane.setLeftAnchor(delBtn, 80.0);
                    setGraphic(wrapper);
                }
            }
        });
    }

    private void loadGroups() {
        User current = SessionManager.getInstance().getCurrentUser();
        int teacherId = current != null ? current.getId() : 1;
        List<Group> groups = groupDAO.getGroupsByTeacher(teacherId);
        if (groupListView != null) {
            groupListView.getItems().setAll(groups);
        } else if (groupsTable != null) {
            groupsTable.getItems().setAll(groups);
        }
    }

    private void openGroupForm(Group g) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GroupForm.fxml"));
            AnchorPane pane = loader.load();
            GroupFormController controller = loader.getController();
            controller.setEditingGroup(g);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(g == null ? "Agregar Grupo" : "Editar Grupo");
            stage.setScene(new Scene(pane));
            stage.showAndWait();

            // after closing, reload
            loadGroups();
        } catch (IOException e) {
            showError("Error al abrir formulario de grupo: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewGroup() {
        openGroupForm(null);
    }

    private void handleDelete(Group g) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Eliminar grupo");
        a.setHeaderText("¿Eliminar grupo?");
        a.setContentText("Esta acción eliminará el grupo y sus relaciones.\n" + g.getName());
        a.showAndWait().ifPresent(bt -> {
            if (bt == javafx.scene.control.ButtonType.OK) {
                if (groupDAO.deleteGroup(g.getId())) {
                    loadGroups();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Error al eliminar grupo");
                    err.showAndWait();
                }
            }
        });
    }

    private void loadStudentsCards(int groupId) {
        studentsFlow.getChildren().clear();
        List<GroupStudent> students = groupStudentDAO.getStudentsByGroup(groupId);
        for (GroupStudent s : students) {
            VBox card = createStudentCard(s);
            card.setOpacity(0);
            studentsFlow.getChildren().add(card);
            // fade-in
            FadeTransition ft = new FadeTransition(Duration.millis(350), card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    private VBox createStudentCard(GroupStudent s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentCard.fxml"));
            VBox card = loader.load();
            com.edutrack.controller.StudentCardController controller = loader.getController();
            controller.setStudent(s);
            // set remove handler with fade-out animation
            controller.setOnRemove(() -> {
                FadeTransition ft = new FadeTransition(Duration.millis(300), card);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(ev -> {
                    if (groupStudentDAO.removeStudentFromGroup(s.getId())) {
                        loadStudentsCards(s.getGroupId());
                    } else {
                        // if remove failed, restore opacity
                        card.setOpacity(1);
                        showError("Error al eliminar alumno del grupo");
                    }
                });
                ft.play();
            });
            return card;
        } catch (IOException e) {
            e.printStackTrace();
            // fallback to simple card
            VBox card = new VBox(8);
            card.getStyleClass().addAll("student-card");
            card.setPrefWidth(200);
            card.setAlignment(Pos.TOP_LEFT);
            Label name = new Label(s.getStudentName());
            Label id = new Label("ID: " + s.getStudentId());
            Button remove = new Button("Eliminar");
            remove.getStyleClass().add("danger-button");
            remove.setOnAction(ev -> { if (groupStudentDAO.removeStudentFromGroup(s.getId())) loadStudentsCards(s.getGroupId()); });
            card.getChildren().addAll(new HBox(new Label(s.getStudentName()), new Label(" ")), id, remove);
            return card;
        }
    }

    @FXML
    private void handleAddStudentToGroup() {
        if (selectedGroup == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleccione un grupo primero");
            a.showAndWait();
            return;
        }

        try {
            // Reuse a simple dialog to pick a student (using a ComboBox to allow custom cells)
            List<com.edutrack.model.User> allStudents = new com.edutrack.database.UserDAO().getAllStudents();

            javafx.scene.control.Dialog<com.edutrack.model.User> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Agregar alumno");
            dialog.setHeaderText("Seleccione un alumno para agregar al grupo");

            javafx.scene.control.ButtonType addBtn = new javafx.scene.control.ButtonType("Agregar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addBtn, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.control.ComboBox<com.edutrack.model.User> combo = new javafx.scene.control.ComboBox<>(
                    javafx.collections.FXCollections.observableArrayList(allStudents)
            );
            combo.setPrefWidth(360);
            combo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(com.edutrack.model.User item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item.getFullName() != null && !item.getFullName().trim().isEmpty() ? item.getFullName() : item.getUsername());
                }
            });
            combo.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(com.edutrack.model.User item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item.getFullName() != null && !item.getFullName().trim().isEmpty() ? item.getFullName() : item.getUsername());
                }
            });
            if (!allStudents.isEmpty()) combo.getSelectionModel().selectFirst();

            dialog.getDialogPane().setContent(combo);
            dialog.setResultConverter(bt -> bt == addBtn ? combo.getSelectionModel().getSelectedItem() : null);

            java.util.Optional<com.edutrack.model.User> res = dialog.showAndWait();
            res.ifPresent(user -> {
                if (groupStudentDAO.addStudentToGroup(selectedGroup.getId(), user.getId())) {
                    loadStudentsCards(selectedGroup.getId());
                    // update students count in table
                    loadGroups();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Error al agregar alumno (quizás ya está en el grupo)");
                    err.showAndWait();
                }
            });

        } catch (Exception e) {
            showError("Error al agregar alumno: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSelectedGroup() {
        Group g = selectedGroup;
        if (g == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleccione un grupo primero");
            a.showAndWait();
            return;
        }
        handleDelete(g);
    }

    @FXML
    private void handleTakeAttendance() {
        Group g = selectedGroup;
        if (g == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleccione un grupo primero para pasar lista");
            a.showAndWait();
            return;
        }
        // Reuse TeacherDashboardController logic simplified here: open dialog for present/absent
        try {
            List<GroupStudent> students = groupStudentDAO.getStudentsByGroup(g.getId());
            if (students.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "No hay alumnos en este grupo");
                a.showAndWait();
                return;
            }
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Pasar Lista");
            dialog.setHeaderText("Grupo: " + g.getName());
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            ListView<CheckBox> attendanceList = new ListView<>();
            students.forEach(s -> {
                CheckBox cb = new CheckBox(s.getStudentName());
                cb.setSelected(true);
                cb.setUserData(s.getStudentId());
                attendanceList.getItems().add(cb);
            });
            dialog.getDialogPane().setContent(attendanceList);
            java.util.Optional<Void> res = dialog.showAndWait();
            if (res.isPresent()) {
                attendanceList.getItems().forEach(cb -> {
                    int sid = (int) cb.getUserData();
                    boolean present = cb.isSelected();
                    new com.edutrack.database.AttendanceDAO().recordAttendance(g.getId(), sid, present);
                });
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Asistencia registrada");
                info.showAndWait();
            }

        } catch (Exception e) {
            showError("Error al pasar lista: " + e.getMessage());
        }
    }

    public void selectGroupById(int groupId) {
        // ensure groups are loaded
        loadGroups();
        if (groupListView != null) {
            for (int i = 0; i < groupListView.getItems().size(); i++) {
                Group g = groupListView.getItems().get(i);
                if (g != null && g.getId() == groupId) {
                    groupListView.getSelectionModel().select(i);
                    groupListView.scrollTo(i);
                    selectedGroup = g;
                    selectedGroupLabel.setText(g.getName());
                    loadStudentsCards(g.getId());
                    break;
                }
            }
        } else if (groupsTable != null) {
            for (int i = 0; i < groupsTable.getItems().size(); i++) {
                Group g = groupsTable.getItems().get(i);
                if (g != null && g.getId() == groupId) {
                    groupsTable.getSelectionModel().select(i);
                    groupsTable.scrollTo(i);
                    selectedGroup = g;
                    selectedGroupLabel.setText(g.getName());
                    loadStudentsCards(g.getId());
                    break;
                }
            }
        }
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}