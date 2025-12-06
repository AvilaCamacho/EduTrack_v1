package com.edutrack.model;

public class GroupStudent {
    private int id;
    private int groupId;
    private int studentId;
    private String studentName;

    public GroupStudent() {
    }

    public GroupStudent(int id, int groupId, int studentId, String studentName) {
        this.id = id;
        this.groupId = groupId;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
