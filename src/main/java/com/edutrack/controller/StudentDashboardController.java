package com.edutrack.controller;

import com.edutrack.database.AttendanceDAO;
import com.edutrack.database.GroupDAO;
import com.edutrack.model.Attendance;
import com.edutrack.model.Group;
import com.edutrack.model.User;
import com.edutrack.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private TableColumn<Attendance, String> noteColumn;

    @FXML
    private Label attendancePercentLabel;

    @FXML
    private TextArea historyTextArea;

    @FXML
    private Label groupAverageLabel;

    @FXML
    private Button calcPercentBtn;

    @FXML
    private Button clearHistoryBtn;

    @FXML
    private Button recordAttendanceBtn;

    @FXML
    private Button exportHistoryBtn;

    @FXML
    private Button openRollBtn;

    @FXML
    private Button openCalendarBtn;

    @FXML
    private Button contactTeacherBtn;

    // Nuevos campos para evitar unresolved fx:id
    @FXML
    private Button profileBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button viewGroupBtn;

    @FXML
    private Button toggleGroupBtn;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentEmailLabel;

    private final GroupDAO groupDAO;
    private final AttendanceDAO attendanceDAO;
    private User currentUser;

    public StudentDashboardController() {
        groupDAO = new GroupDAO();
        attendanceDAO = new AttendanceDAO();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenido, " + currentUser.getFullName());
            studentNameLabel.setText(currentUser.getFullName());
            studentEmailLabel.setText(currentUser.getEmail());
        } else {
            welcomeLabel.setText("Bienvenido/a");
        }

        dateColumn.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = sdf.format(cellData.getValue().getAttendanceDate());
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        loadGroups();
        loadAttendance();

        // handlers
        calcPercentBtn.setOnAction(e -> calculateAttendancePercent());
        clearHistoryBtn.setOnAction(e -> historyTextArea.clear());
        recordAttendanceBtn.setOnAction(e -> quickRecordAttendance());
        openRollBtn.setOnAction(e -> openRoll());
        exportHistoryBtn.setOnAction(e -> exportAttendance());
        openCalendarBtn.setOnAction(e -> openCalendar());
        contactTeacherBtn.setOnAction(e -> contactTeacher());

        profileBtn.setOnAction(e -> openProfile());
        logoutBtn.setOnAction(e -> handleLogout());
        viewGroupBtn.setOnAction(e -> viewSelectedGroup());
        toggleGroupBtn.setOnAction(e -> toggleGroupEnrollment());

        // Mejorar UX: al seleccionar grupo, cargar asistencia asociada
        groupListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                loadAttendanceForGroup(newV.getId());
            } else {
                loadAttendance();
            }
        });
    }

    private void loadGroups() {
        if (currentUser == null) return;
        List<Group> groups = groupDAO.getGroupsByStudent(currentUser.getId());
        groupListView.getItems().setAll(groups);
    }

    private void loadAttendance() {
        if (currentUser == null) return;
        List<Attendance> attendance = attendanceDAO.getAttendanceByStudent(currentUser.getId());
        attendanceTable.getItems().setAll(attendance);
        updateAttendancePercentLabel(attendance);
    }

    private void loadAttendanceForGroup(int groupId) {
        if (currentUser == null) return;
        // usa la fecha más reciente disponible para el grupo
        List<java.sql.Date> dates = attendanceDAO.getAttendanceDatesByGroup(groupId);
        if (dates == null || dates.isEmpty()) {
            attendanceTable.getItems().clear();
            attendancePercentLabel.setText("0%");
            return;
        }
        java.sql.Date latest = dates.get(0); // DAO devuelve en orden descendente
        List<Attendance> attendance = attendanceDAO.getAttendanceByGroup(groupId, latest);
        attendanceTable.getItems().setAll(attendance);
        updateAttendancePercentLabel(attendance);
    }

    private void updateAttendancePercentLabel(List<Attendance> attendance) {
        if (attendance == null || attendance.isEmpty()) {
            attendancePercentLabel.setText("0%");
            return;
        }
        long total = attendance.size();
        long present = attendance.stream().filter(Attendance::isPresent).count();
        int percent = (int) Math.round((present * 100.0) / total);
        attendancePercentLabel.setText(percent + "%");
    }

    private void calculateAttendancePercent() {
        List<Attendance> items = attendanceTable.getItems();
        updateAttendancePercentLabel(items);

        // Calcular media del grupo: para cada grupo tomar la fecha más reciente y calcular % presente
        List<Group> groups = groupListView.getItems();
        if (groups == null || groups.isEmpty()) {
            groupAverageLabel.setText("--");
            return;
        }

        List<Integer> percents = groups.stream().map(g -> {
            List<java.sql.Date> dates = attendanceDAO.getAttendanceDatesByGroup(g.getId());
            if (dates == null || dates.isEmpty()) return null;
            java.sql.Date latest = dates.get(0);
            List<Attendance> atts = attendanceDAO.getAttendanceByGroup(g.getId(), latest);
            if (atts == null || atts.isEmpty()) return null;
            long total = atts.size();
            long present = atts.stream().filter(Attendance::isPresent).count();
            return (int) Math.round((present * 100.0) / total);
        }).filter(p -> p != null).collect(Collectors.toList());

        if (percents.isEmpty()) {
            groupAverageLabel.setText("--");
            return;
        }
        double avg = percents.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN);
        if (Double.isNaN(avg)) groupAverageLabel.setText("--"); else groupAverageLabel.setText(String.format("%d%%", Math.round(avg)));
    }

    private void quickRecordAttendance() {
        if (currentUser == null) return;
        Group g = groupListView.getSelectionModel().getSelectedItem();
        if (g == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Seleccione un grupo para registrar asistencia.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        boolean ok = attendanceDAO.recordAttendance(g.getId(), currentUser.getId(), true);
        if (!ok) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo registrar la asistencia.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        // refrescar UI para el grupo
        loadAttendanceForGroup(g.getId());
    }

    private void openRoll() {
        // Simulación: expandir la sección Asistencia
        Platform.runLater(() -> {
            // abrir TitledPane de Asistencia si existe
        });
    }

    private void exportAttendance() {
        // Functionalidad mínima: volcar a text area
        StringBuilder sb = new StringBuilder();
        for (Attendance a : attendanceTable.getItems()) {
            sb.append(a.getAttendanceDate()).append(" - ").append(a.getStatus()).append(" - ").append(a.getStudentName()).append('\n');
        }
        historyTextArea.setText(sb.toString());
    }

    private void openCalendar() {
        // placeholder
    }

    private void contactTeacher() {
        // placeholder
    }

    private void openProfile() {
        // placeholder: abrir diálogo de perfil
    }

    private void handleLogout() {
        // placeholder: limpiar sesión y volver al login
        SessionManager.getInstance().clear();
        // lógica para volver a la pantalla de login normalmente la realiza la clase Main
    }

    private void viewSelectedGroup() {
        Group g = groupListView.getSelectionModel().getSelectedItem();
        if (g == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Seleccione un grupo primero.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        // mostrar detalles mínimos
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Grupo: " + g.getName(), ButtonType.OK);
        a.showAndWait();
    }

    private void toggleGroupEnrollment() {
        Group g = groupListView.getSelectionModel().getSelectedItem();
        if (g == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Seleccione un grupo primero.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        // Simulación: alternar inscripción
        boolean enrolled = groupDAO.isStudentInGroup(currentUser.getId(), g.getId());
        if (enrolled) {
            groupDAO.removeStudentFromGroup(currentUser.getId(), g.getId());
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Has salido del grupo.", ButtonType.OK);
            a.showAndWait();
        } else {
            groupDAO.addStudentToGroup(currentUser.getId(), g.getId());
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Te has unido al grupo.", ButtonType.OK);
            a.showAndWait();
        }
        loadGroups();
    }
}