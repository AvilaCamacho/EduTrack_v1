package com.edutrack.model;

import java.util.Date;

public class Group {
    private int id;
    private String name;
    private String description;
    private int teacherId;
    private Date createdDate;

    public Group() {
    }

    public Group(int id, String name, String description, int teacherId, Date createdDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.teacherId = teacherId;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return name != null && !name.isEmpty() ? name : "Grupo #" + id;
    }
}