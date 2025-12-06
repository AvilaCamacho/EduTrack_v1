package com.edutrack.database;

import com.edutrack.model.Group;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {
    
    public List<Group> getGroupsByTeacher(int teacherId) {
        List<Group> groups = new ArrayList<>();
        String query = "SELECT * FROM groups WHERE teacher_id = ? ORDER BY created_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Group group = new Group();
                group.setId(rs.getInt("id"));
                group.setName(rs.getString("name"));
                group.setDescription(rs.getString("description"));
                group.setTeacherId(rs.getInt("teacher_id"));
                group.setCreatedDate(rs.getDate("created_date"));
                groups.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching groups: " + e.getMessage());
        }
        
        return groups;
    }

    public List<Group> getGroupsByStudent(int studentId) {
        List<Group> groups = new ArrayList<>();
        String query = "SELECT g.* FROM groups g " +
                      "JOIN group_students gs ON g.id = gs.group_id " +
                      "WHERE gs.student_id = ? ORDER BY g.created_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Group group = new Group();
                group.setId(rs.getInt("id"));
                group.setName(rs.getString("name"));
                group.setDescription(rs.getString("description"));
                group.setTeacherId(rs.getInt("teacher_id"));
                group.setCreatedDate(rs.getDate("created_date"));
                groups.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student groups: " + e.getMessage());
        }
        
        return groups;
    }

    public boolean createGroup(Group group) {
        String query = "INSERT INTO groups (name, description, teacher_id, created_date) VALUES (?, ?, ?, SYSDATE)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setInt(3, group.getTeacherId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating group: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGroup(int groupId) {
        String query = "DELETE FROM groups WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, groupId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting group: " + e.getMessage());
            return false;
        }
    }
}
