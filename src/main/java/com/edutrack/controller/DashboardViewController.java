package com.edutrack.controller;

import com.edutrack.database.GroupDAO;
import com.edutrack.database.GroupStudentDAO;
import com.edutrack.model.Group;
import com.edutrack.model.GroupStudent;
import com.edutrack.util.SessionManager;
import com.edutrack.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.InputStream;
import java.util.List;

public class DashboardViewController {
    @FXML private HBox metricsBox;
    @FXML private ListView<Group> groupsListView;
    @FXML private Pane chartPane;
    @FXML private Label welcomeLabel;
    @FXML private Button heroAttendanceBtn;
    @FXML private ImageView logoImage;

    private final GroupDAO groupDAO = new GroupDAO();
    private final GroupStudentDAO groupStudentDAO = new GroupStudentDAO();

    @FXML
    private void initialize() {
        User current = SessionManager.getInstance().getCurrentUser();
        String name = (current != null && current.getFullName() != null) ? current.getFullName() : "Profesor";
        if (welcomeLabel != null) welcomeLabel.setText("Bienvenida, Prof. " + name + "!");

        // cargar imagen de logo si está disponible
        try (InputStream is = getClass().getResourceAsStream("/images/login1.jpg")) {
            if (is != null && logoImage != null) {
                Image img = new Image(is);
                logoImage.setImage(img);
                logoImage.setPreserveRatio(true);
                logoImage.setFitHeight(40);
            }
        } catch (Exception ex) {
            // no crítico
        }

        // crear métricas de ejemplo
        VBox card1 = createMetric("Estudiantes Presentes", "28");
        VBox card2 = createMetric("Estudiantes Ausentes", "4");
        VBox card3 = createMetric("Retardos", "2");
        metricsBox.getChildren().addAll(card1, card2, card3);

        // cargar grupos
        if (groupsListView != null) {
            List<Group> plain = groupDAO.getGroupsByTeacher(current == null ? 1 : current.getId());
            ObservableList<Group> obs = FXCollections.observableArrayList();
            if (plain != null && !plain.isEmpty()) obs.addAll(plain);
            groupsListView.setItems(obs);
        }

        if (heroAttendanceBtn != null) {
            heroAttendanceBtn.setOnAction(e -> openAttendanceChooser());
        }
    }

    private void openAttendanceChooser() {
        // dialog para elegir grupo y luego abrir el pase de lista
        ObservableList<Group> groups;
        if (groupsListView != null) {
            groups = groupsListView.getItems();
        } else {
            groups = FXCollections.observableArrayList();
            List<Group> plain = groupDAO.getGroupsByTeacher(1);
            if (plain != null && !plain.isEmpty()) groups.addAll(plain);
        }

        Dialog<Group> d = new Dialog<>();
        d.setTitle("Tomar Asistencia");
        d.getDialogPane().getButtonTypes().addAll(new ButtonType("Seleccionar", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);

        javafx.scene.control.ComboBox<Group> combo = new javafx.scene.control.ComboBox<>(groups);
        combo.setCellFactory(lv -> new javafx.scene.control.ListCell<>(){
            @Override protected void updateItem(Group item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null?null:item.getName()); }
        });
        if (!groups.isEmpty()) combo.getSelectionModel().selectFirst();
        d.getDialogPane().setContent(combo);
        d.setResultConverter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE ? combo.getSelectionModel().getSelectedItem() : null);

        d.showAndWait().ifPresent(g -> {
            if (g != null) {
                openAttendanceDialogForGroup(g);
            }
        });
    }

    private void openAttendanceDialogForGroup(Group g) {
        try {
            List<GroupStudent> students = groupStudentDAO.getStudentsByGroup(g.getId());
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Pasar Lista");
            dialog.setHeaderText("Grupo: " + g.getName());
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            ListView<CheckBox> attendanceList = new ListView<>();
            if (students != null) {
                students.forEach(s -> {
                    CheckBox cb = new CheckBox(s.getStudentName());
                    cb.setSelected(true);
                    cb.setUserData(s.getStudentId());
                    attendanceList.getItems().add(cb);
                });
            }
            dialog.getDialogPane().setContent(attendanceList);
            dialog.showAndWait().ifPresent(res -> {
                attendanceList.getItems().forEach(cb -> {
                    int sid = (int) cb.getUserData();
                    boolean present = cb.isSelected();
                    new com.edutrack.database.AttendanceDAO().recordAttendance(g.getId(), sid, present);
                });
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Asistencia registrada");
                info.showAndWait();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Error al abrir pase de lista: " + e.getMessage());
            a.showAndWait();
        }
    }

    private VBox createMetric(String title, String value) {
        VBox v = new VBox(6);
        v.getStyleClass().addAll("metric-card");
        Label t = new Label(title);
        t.getStyleClass().add("metric-label");
        Label val = new Label(value);
        val.getStyleClass().add("metric-value");
        v.getChildren().addAll(t, val);
        v.setPrefWidth(240);
        return v;
    }
}