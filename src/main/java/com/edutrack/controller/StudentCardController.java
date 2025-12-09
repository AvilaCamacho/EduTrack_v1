package com.edutrack.controller;

import com.edutrack.model.GroupStudent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StudentCardController {

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label metaLabel;
    @FXML private Button removeBtn;

    private GroupStudent student;
    private Runnable onRemove;

    public void setStudent(GroupStudent s) {
        this.student = s;
        nameLabel.setText(s.getStudentName());
        metaLabel.setText("ID: " + s.getStudentId());
        String initials = "";
        if (s.getStudentName() != null && !s.getStudentName().isBlank()) {
            StringBuilder sb = new StringBuilder();
            String[] parts = s.getStudentName().split(" ");
            for (String p : parts) if (!p.isBlank()) sb.append(p.charAt(0));
            initials = sb.toString();
            if (initials.length() > 2) initials = initials.substring(0,2);
        }
        avatarLabel.setText(initials.isBlank() ? "?" : initials.toUpperCase());

        removeBtn.setOnAction(ev -> {
            if (onRemove != null) onRemove.run();
        });
    }

    public void setOnRemove(Runnable r) { this.onRemove = r; }
}