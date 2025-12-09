package com.edutrack.model;

public class User {
    private int id;
    private String username;
    private String password;
    private UserType userType;
    private String fullName;
    private String email;

    public enum UserType {
        TEACHER,
        STUDENT
    }

    public User() {
    }

    public User(int id, String username, String password, UserType userType, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.fullName = fullName;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return fullName != null && !fullName.isEmpty() ? fullName : username != null ? username : "Usuario #" + id;
    }
}