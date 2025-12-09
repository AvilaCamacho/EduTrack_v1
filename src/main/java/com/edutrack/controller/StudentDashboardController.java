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
