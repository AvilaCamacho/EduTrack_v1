package com.edutrack.controller;

import com.edutrack.database.AttendanceDAO;
import com.edutrack.database.GroupDAO;
import com.edutrack.model.Attendance;
import com.edutrack.model.Group;
import com.edutrack.model.User;
import com.edutrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

public class StudentDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<Group> groupListView;

    @FXML
    private TableView<Attendance> attendanceTable;

    @FXML
    private TableColumn<Attendance, String> dateColumn;

    @FXML
    private TableColumn<Attendance, String> statusColumn;

    @FXML
    private ImageView logoImage;

    private GroupDAO groupDAO;
    private AttendanceDAO attendanceDAO;
    private User currentUser;

    public StudentDashboardController() {
        groupDAO = new GroupDAO();
        attendanceDAO = new AttendanceDAO();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Bienvenido, " + currentUser.getFullName());

        // Load logo image (use login1.jpg as default)
        try (InputStream is = getClass().getResourceAsStream("/images/login1.jpg")) {
            if (is != null && logoImage != null) {
                Image img = new Image(is);
                logoImage.setImage(img);
                logoImage.setPreserveRatio(true);
                logoImage.setFitHeight(48);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar logo: " + e.getMessage());
        }

        // Mostrar el nombre del grupo (no la referencia del objeto)
        groupListView.setCellFactory(list -> new ListCell<Group>() {
            @Override
            protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String name = item.getName();
                    setText((name == null || name.trim().isEmpty()) ? item.toString() : name);
                }
            }
        });

        dateColumn.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = sdf.format(cellData.getValue().getAttendanceDate());
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadGroups();
        loadAttendance();
    }

    private void loadGroups() {
        List<Group> groups = groupDAO.getGroupsByStudent(currentUser.getId());
        groupListView.getItems().setAll(groups);
    }

    private void loadAttendance() {
        List<Attendance> attendance = attendanceDAO.getAttendanceByStudent(currentUser.getId());
        attendanceTable.getItems().setAll(attendance);
    }
}