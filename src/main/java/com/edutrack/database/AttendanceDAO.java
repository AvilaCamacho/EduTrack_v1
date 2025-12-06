package com.edutrack.database;

import com.edutrack.model.Attendance;

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

    public boolean recordAttendance(int groupId, int studentId, boolean present) {
        String query = "INSERT INTO attendance (group_id, student_id, attendance_date, present) " +
                      "VALUES (?, ?, SYSDATE, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, groupId);
            stmt.setInt(2, studentId);
            stmt.setBoolean(3, present);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
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
}
