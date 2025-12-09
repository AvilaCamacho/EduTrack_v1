package com.edutrack.controller;

import com.edutrack.database.*;
import com.edutrack.model.*;
import com.edutrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

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

    @FXML
    private Button viewHistoryButton;

    @FXML
    private HBox studentActions; // contenedor de acciones de estudiantes

    @FXML
    private Label noSelectionLabel;

    @FXML
    private Button generateAttendanceButton; // nuevo botón

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
        System.out.println("TeacherDashboardController: initialize called");

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenido, " + currentUser.getFullName());
            } else {
                System.err.println("welcomeLabel es null en TeacherDashboardController");
            }
        } else {
            if (welcomeLabel != null) welcomeLabel.setText("Bienvenido");
        }

        if (studentNameColumn != null) {
            studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            // Make table column resize to fill available space for responsive layout
            if (studentsTable != null) {
                studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            }
            studentNameColumn.setMaxWidth(Double.MAX_VALUE);
        } else {
            System.err.println("studentNameColumn es null en TeacherDashboardController");
        }

        // Ensure group list expands
        if (groupListView != null) {
            groupListView.setPrefWidth(Double.MAX_VALUE);
            groupListView.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        } else {
            System.err.println("groupListView es null en TeacherDashboardController");
        }

        // Start with actions hidden
        if (studentActions != null) {
            studentActions.setVisible(false);
            studentActions.setManaged(false);
        } else {
            System.err.println("studentActions es null en TeacherDashboardController");
        }

        if (noSelectionLabel != null) {
            noSelectionLabel.setVisible(true);
        } else {
            System.err.println("noSelectionLabel es null en TeacherDashboardController");
        }

        if (groupListView != null) {
            groupListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> onGroupSelectionChanged(newValue)
            );
        } else {
            System.err.println("groupListView es null en TeacherDashboardController");
        }

        loadGroups();
    }

    private void onGroupSelectionChanged(Group selectedGroup) {
        if (selectedGroup == null) {
            studentsTable.getItems().clear();
            studentActions.setVisible(false);
            studentActions.setManaged(false);
            noSelectionLabel.setVisible(true);
            deleteGroupButton.setDisable(true);
            return;
        }

        // Enable group-related actions
        studentActions.setVisible(true);
        studentActions.setManaged(true);
        noSelectionLabel.setVisible(false);
        deleteGroupButton.setDisable(false);

        loadStudentsForGroup(selectedGroup);
    }

    private void loadGroups() {
        if (currentUser == null) return;
        List<Group> groups = groupDAO.getGroupsByTeacher(currentUser.getId());
        groupListView.getItems().setAll(groups);

        // disable delete/create buttons if no groups exist
        boolean hasGroups = !groups.isEmpty();
        deleteGroupButton.setDisable(!hasGroups);
    }

    private void loadStudentsForGroup(Group group) {
        if (group == null) {
            studentsTable.getItems().clear();
            return;
        }

        List<GroupStudent> students = groupStudentDAO.getStudentsByGroup(group.getId());
        studentsTable.getItems().setAll(students);

        // disable removeStudentButton if no students
        removeStudentButton.setDisable(students.isEmpty());
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
                if (currentUser != null) newGroup.setTeacherId(currentUser.getId());

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

    @FXML
    private void handleViewHistory() {
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            return;
        }

        // Obtener fechas disponibles
        List<java.sql.Date> dates = attendanceDAO.getAttendanceDatesByGroup(selectedGroup.getId());
        if (dates.isEmpty()) {
            showInfo("No hay historial de asistencia para este grupo");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de Asistencia - " + selectedGroup.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        // Left: lista de fechas, Right: tabla de asistencia
        HBox content = new HBox(10);
        content.setPrefSize(700, 400);

        // Panel izquierdo: filtros + lista de fechas
        javafx.scene.layout.VBox leftPanel = new javafx.scene.layout.VBox(10);

        DatePicker dpFilterStart = new DatePicker();
        DatePicker dpFilterEnd = new DatePicker();
        Button btnFilter = new Button("Filtrar");

        HBox filterBox = new HBox(10);
        filterBox.getChildren().addAll(new Label("Inicio:"), dpFilterStart, new Label("Fin:"), dpFilterEnd, btnFilter);

        ListView<java.sql.Date> datesList = new ListView<>();
        datesList.getItems().addAll(dates);

        // Formateador para mostrar fechas legibles
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        datesList.setCellFactory(lv -> new javafx.scene.control.ListCell<java.sql.Date>() {
            @Override
            protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(sdf.format(new java.util.Date(item.getTime())));
            }
        });

        btnFilter.setOnAction(e -> {
            java.time.LocalDate s = dpFilterStart.getValue();
            java.time.LocalDate en = dpFilterEnd.getValue();
            datesList.getItems().clear();
            if (s == null && en == null) {
                datesList.getItems().addAll(dates);
                return;
            }
            for (java.sql.Date d : dates) {
                java.time.LocalDate ld = d.toLocalDate();
                boolean afterOrEq = (s == null) || (!ld.isBefore(s));
                boolean beforeOrEq = (en == null) || (!ld.isAfter(en));
                if (afterOrEq && beforeOrEq) datesList.getItems().add(d);
            }
        });

        leftPanel.getChildren().addAll(filterBox, datesList);

        TableView<Attendance> attendanceTable = new TableView<>();
        TableColumn<Attendance, String> nameCol = new TableColumn<>("Alumno");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStudentName()));
        nameCol.setPrefWidth(300);

        TableColumn<Attendance, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(150);

        TableColumn<Attendance, java.util.Date> dateCol = new TableColumn<>("Fecha");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAttendanceDate()));
        dateCol.setPrefWidth(200);

        attendanceTable.getColumns().addAll(nameCol, statusCol, dateCol);

        datesList.getSelectionModel().selectedItemProperty().addListener((obs, oldD, newD) -> {
            if (newD != null) {
                List<Attendance> list = attendanceDAO.getAttendanceByGroup(selectedGroup.getId(), newD);
                attendanceTable.getItems().setAll(list);
            }
        });

        // select first by default and force load
        if (!dates.isEmpty()) {
            datesList.getSelectionModel().select(0);
            java.sql.Date first = datesList.getSelectionModel().getSelectedItem();
            if (first != null) {
                List<Attendance> list = attendanceDAO.getAttendanceByGroup(selectedGroup.getId(), first);
                attendanceTable.getItems().setAll(list);
            }
        }

        content.getChildren().addAll(leftPanel, attendanceTable);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @FXML
    private void handleGenerateAttendanceGrade() {
        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Por favor seleccione un grupo");
            return;
        }

        // diálogo con dos DatePicker
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generar Calificación de Asistencia");
        dialog.setHeaderText("Seleccione rango de fechas");

        DatePicker dpStart = new DatePicker();
        DatePicker dpEnd = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Fecha inicio:"), 0, 0);
        grid.add(dpStart, 1, 0);
        grid.add(new Label("Fecha fin:"), 0, 1);
        grid.add(dpEnd, 1, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType generateType = new ButtonType("Generar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateType, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == generateType) {
            java.time.LocalDate start = dpStart.getValue();
            java.time.LocalDate end = dpEnd.getValue();
            if (start == null || end == null) {
                showError("Debe seleccionar ambas fechas");
                return;
            }
            if (start.isAfter(end)) {
                showError("La fecha de inicio no puede ser posterior a la fecha fin");
                return;
            }

            // Convertir a java.sql.Date
            Date sqlStart = Date.valueOf(start);
            Date sqlEnd = Date.valueOf(end);

            // Llamar al DAO para generar reporte
            List<com.edutrack.model.AttendanceReportEntry> report = attendanceDAO.generateAttendanceReport(selectedGroup.getId(), sqlStart, sqlEnd);

            if (report == null || report.isEmpty()) {
                showInfo("No hay días de clase en el rango seleccionado o no hay alumnos");
                return;
            }

            // mostrar resultados en tabla
            Dialog<Void> resDialog = new Dialog<>();
            resDialog.setTitle("Resultado: Calificación de Asistencia");
            resDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

            TableView<com.edutrack.model.AttendanceReportEntry> table = new TableView<>();
            TableColumn<com.edutrack.model.AttendanceReportEntry, String> nameCol = new TableColumn<>("Alumno");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            nameCol.setPrefWidth(300);

            TableColumn<com.edutrack.model.AttendanceReportEntry, Integer> presentCol = new TableColumn<>("Presentes");
            presentCol.setCellValueFactory(new PropertyValueFactory<>("presentDays"));
            presentCol.setPrefWidth(100);

            TableColumn<com.edutrack.model.AttendanceReportEntry, Integer> totalCol = new TableColumn<>("Días totales");
            totalCol.setCellValueFactory(new PropertyValueFactory<>("totalDays"));
            totalCol.setPrefWidth(100);

            TableColumn<com.edutrack.model.AttendanceReportEntry, String> percCol = new TableColumn<>("% Asistencia");
            percCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("%.1f %%", c.getValue().getPercentage())));
            percCol.setPrefWidth(120);

            table.getColumns().addAll(nameCol, presentCol, totalCol, percCol);
            table.getItems().setAll(report);

            Button exportBtn = new Button("Exportar CSV");
            exportBtn.setOnAction(e -> {
                try {
                    String csv = report.stream()
                            .map(r -> String.format("\"%s\",%d,%d,%.1f", r.getStudentName(), r.getPresentDays(), r.getTotalDays(), r.getPercentage()))
                            .collect(Collectors.joining("\n"));
                    // Guardar en archivo sencillo en el directorio del usuario
                    String userHome = System.getProperty("user.home");
                    String path = userHome + "\\attendance_report.csv";
                    try (FileWriter fw = new FileWriter(path)) {
                        fw.write("Alumno,Presentes,TotalDias,Porcentaje\n");
                        fw.write(csv);
                    }
                    showInfo("Exportado CSV: " + path);
                } catch (IOException ex) {
                    showError("Error exportando CSV: " + ex.getMessage());
                }
            });

            VBox content = new VBox(10);
            content.setPrefSize(600, 400);
            content.getChildren().addAll(table, exportBtn);

            resDialog.getDialogPane().setContent(content);
            resDialog.showAndWait();
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