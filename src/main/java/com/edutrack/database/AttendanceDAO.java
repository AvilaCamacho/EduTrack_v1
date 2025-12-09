package com.edutrack.database;

import com.edutrack.model.Attendance;
import com.edutrack.model.AttendanceReportEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public List<Attendance> getAttendanceByGroup(int groupId, Date date) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT a.*, u.full_name as student_name " +
                      "FROM attendance a " +
                      "JOIN users u ON a.student_id = u.id " +
                      "WHERE a.group_id = ? AND TRUNC(a.attendance_date) = TRUNC(?) " +
                      "ORDER BY u.full_name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, groupId);
            stmt.setDate(2, date);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setGroupId(rs.getInt("group_id"));
                attendance.setStudentId(rs.getInt("student_id"));
                attendance.setAttendanceDate(rs.getDate("attendance_date"));
                attendance.setPresent(rs.getBoolean("present"));
                attendance.setStudentName(rs.getString("student_name"));
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
        }

        return attendanceList;
    }

    public List<Attendance> getAttendanceByStudent(int studentId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT a.*, g.name as group_name " +
                      "FROM attendance a " +
                      "JOIN groups g ON a.group_id = g.id " +
                      "WHERE a.student_id = ? ORDER BY a.attendance_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setGroupId(rs.getInt("group_id"));
                attendance.setStudentId(rs.getInt("student_id"));
                attendance.setAttendanceDate(rs.getDate("attendance_date"));
                attendance.setPresent(rs.getBoolean("present"));
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student attendance: " + e.getMessage());
        }

        return attendanceList;
    }

    /**
     * Record attendance for a student on the current date (upsert behavior).
     * If a record for the same group/student and today's date exists, update it.
     * Otherwise insert a new record.
     */
    public boolean recordAttendance(int groupId, int studentId, boolean present) {
        String selectQuery = "SELECT id FROM attendance WHERE group_id = ? AND student_id = ? AND TRUNC(attendance_date) = TRUNC(SYSDATE)";
        String insertQuery = "INSERT INTO attendance (group_id, student_id, attendance_date, present) VALUES (?, ?, TRUNC(SYSDATE), ?)";
        String updateQuery = "UPDATE attendance SET present = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // check if exists
            try (PreparedStatement sel = conn.prepareStatement(selectQuery)) {
                sel.setInt(1, groupId);
                sel.setInt(2, studentId);
                ResultSet rs = sel.executeQuery();
                if (rs.next()) {
                    int attendanceId = rs.getInt("id");
                    try (PreparedStatement upd = conn.prepareStatement(updateQuery)) {
                        upd.setBoolean(1, present);
                        upd.setInt(2, attendanceId);
                        int rows = upd.executeUpdate();
                        return rows > 0;
                    }
                }
            }

            // insert if not exists
            try (PreparedStatement ins = conn.prepareStatement(insertQuery)) {
                ins.setInt(1, groupId);
                ins.setInt(2, studentId);
                ins.setBoolean(3, present);
                int rows = ins.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error recording attendance: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAttendance(int attendanceId, boolean present) {
        String query = "UPDATE attendance SET present = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, present);
            stmt.setInt(2, attendanceId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating attendance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns a list of distinct dates (as java.sql.Date) for which attendance exists for a group.
     */
    public List<Date> getAttendanceDatesByGroup(int groupId) {
        List<Date> dates = new ArrayList<>();
        String query = "SELECT DISTINCT TRUNC(attendance_date) as d FROM attendance WHERE group_id = ? ORDER BY TRUNC(attendance_date) DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("d"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance dates: " + e.getMessage());
        }

        return dates;
    }

    /**
     * Genera un reporte de asistencia por alumno para un grupo en un rango de fechas.
     * Calcula el número de días de clase (fechas distintas con registros) y los días presentes por alumno.
     */
    public List<AttendanceReportEntry> generateAttendanceReport(int groupId, Date start, Date end) {
        List<AttendanceReportEntry> report = new ArrayList<>();

        String totalDaysQuery = "SELECT COUNT(DISTINCT TRUNC(attendance_date)) AS total_days " +
                "FROM attendance " +
                "WHERE group_id = ? AND TRUNC(attendance_date) BETWEEN TRUNC(?) AND TRUNC(?)";

        String perStudentQuery = "WITH total AS (" +
                "  SELECT COUNT(DISTINCT TRUNC(attendance_date)) AS total_days " +
                "  FROM attendance WHERE group_id = ? AND TRUNC(attendance_date) BETWEEN TRUNC(?) AND TRUNC(?)" +
                "), students AS (" +
                "  SELECT gs.student_id, u.full_name " +
                "  FROM group_students gs JOIN users u ON gs.student_id = u.id " +
                "  WHERE gs.group_id = ?" +
                ") SELECT s.student_id, s.full_name, " +
                "  COUNT(DISTINCT CASE WHEN a.present = 1 THEN TRUNC(a.attendance_date) END) AS present_days, t.total_days " +
                "FROM students s LEFT JOIN attendance a ON a.student_id = s.student_id AND a.group_id = ? AND TRUNC(a.attendance_date) BETWEEN TRUNC(?) AND TRUNC(?) " +
                "CROSS JOIN total t " +
                "GROUP BY s.student_id, s.full_name, t.total_days " +
                "ORDER BY s.full_name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // obtener totalDays
            int totalDays = 0;
            try (PreparedStatement tstmt = conn.prepareStatement(totalDaysQuery)) {
                tstmt.setInt(1, groupId);
                tstmt.setDate(2, start);
                tstmt.setDate(3, end);
                ResultSet trs = tstmt.executeQuery();
                if (trs.next()) totalDays = trs.getInt("total_days");
            }

            if (totalDays == 0) {
                return report; // vacío
            }

            try (PreparedStatement pst = conn.prepareStatement(perStudentQuery)) {
                // params for WITH total
                pst.setInt(1, groupId);
                pst.setDate(2, start);
                pst.setDate(3, end);
                // params for students CTE
                pst.setInt(4, groupId);
                // params for LEFT JOIN attendance
                pst.setInt(5, groupId);
                pst.setDate(6, start);
                pst.setDate(7, end);

                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    String fullName = rs.getString("full_name");
                    int presentDays = rs.getInt("present_days");
                    int total = rs.getInt("total_days");
                    double perc = 0.0;
                    if (total > 0) perc = (presentDays * 100.0) / total;

                    AttendanceReportEntry entry = new AttendanceReportEntry(studentId, fullName, presentDays, total, perc);
                    report.add(entry);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error generating attendance report: " + e.getMessage());
        }

        return report;
    }
}