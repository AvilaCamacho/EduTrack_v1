package com.edutrack.model;

import java.util.Date;

public class Attendance {
    private int id;
    private int groupId;
    private int studentId;
    private Date attendanceDate;
    private boolean present;
    private String studentName;

    public Attendance() {
    }

    public Attendance(int id, int groupId, int studentId, Date attendanceDate, boolean present, String studentName) {
        this.id = id;
        this.groupId = groupId;
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.present = present;
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

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return present ? "Presente" : "Ausente";
    }
}
