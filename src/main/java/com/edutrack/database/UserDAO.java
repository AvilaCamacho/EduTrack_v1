package com.edutrack.database;

import com.edutrack.model.User;
import com.edutrack.model.User.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setUserType(UserType.valueOf(rs.getString("user_type")));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user (DB): " + e.getMessage());
            // e.printStackTrace();

            // Fallback: modo demo local para que la app funcione aunque la DB no responda
            User demo = fallbackLocalAuth(username, password);
            if (demo != null) {
                System.out.println("Autenticación en modo demo activada para usuario: " + demo.getUsername());
                return demo;
            }
        }

        return null;
    }

    private User fallbackLocalAuth(String username, String password) {
        // Usuarios demo (NO USAR EN PRODUCCIÓN). Sirve para desarrollo cuando DB no está disponible.
        if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
            User u = new User();
            u.setId(-1);
            u.setUsername("admin");
            u.setFullName("Administrador Demo");
            u.setUserType(UserType.TEACHER);
            u.setEmail("admin@local");
            return u;
        }

        if ("student".equalsIgnoreCase(username) && "student".equals(password)) {
            User u = new User();
            u.setId(-2);
            u.setUsername("student");
            u.setFullName("Alumno Demo");
            u.setUserType(UserType.STUDENT);
            u.setEmail("student@local");
            return u;
        }

        return null;
    }

    public List<User> getAllStudents() {
        List<User> students = new ArrayList<>();
        String query = "SELECT * FROM users WHERE user_type = 'STUDENT' ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User student = new User();
                student.setId(rs.getInt("id"));
                student.setUsername(rs.getString("username"));
                student.setUserType(UserType.STUDENT);
                student.setFullName(rs.getString("full_name"));
                student.setEmail(rs.getString("email"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students: " + e.getMessage());
            // fallback: return empty list (no students)
        }

        return students;
    }

    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setUserType(UserType.valueOf(rs.getString("user_type")));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }

        return null;
    }
}