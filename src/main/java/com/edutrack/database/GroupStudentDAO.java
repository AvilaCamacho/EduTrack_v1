package com.edutrack.database;

import com.edutrack.model.GroupStudent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupStudentDAO {
    
    public List<GroupStudent> getStudentsByGroup(int groupId) {
        List<GroupStudent> students = new ArrayList<>();
        String query = "SELECT gs.*, u.full_name as student_name " +
                      "FROM group_students gs " +
                      "JOIN users u ON gs.student_id = u.id " +
                      "WHERE gs.group_id = ? ORDER BY u.full_name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GroupStudent gs = new GroupStudent();
                gs.setId(rs.getInt("id"));
                gs.setGroupId(rs.getInt("group_id"));
                gs.setStudentId(rs.getInt("student_id"));
                gs.setStudentName(rs.getString("student_name"));
                students.add(gs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group students: " + e.getMessage());
        }
        
        return students;
    }

    public boolean addStudentToGroup(int groupId, int studentId) {
        String query = "INSERT INTO group_students (group_id, student_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, groupId);
            stmt.setInt(2, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding student to group: " + e.getMessage());
            return false;
        }
    }

    public boolean removeStudentFromGroup(int groupStudentId) {
        String query = "DELETE FROM group_students WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, groupStudentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing student from group: " + e.getMessage());
            return false;
        }
    }
}
