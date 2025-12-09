package com.edutrack.model;

public class AttendanceReportEntry {
    private int studentId;
    private String studentName;
    private int presentDays;
    private int totalDays;
    private double percentage;

    public AttendanceReportEntry() {}

    public AttendanceReportEntry(int studentId, String studentName, int presentDays, int totalDays, double percentage) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.presentDays = presentDays;
        this.totalDays = totalDays;
        this.percentage = percentage;
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

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}