package com.example.timesheet.dto;

import java.util.ArrayList;
import java.util.List;

public class TimesheetGridDTO {
    private Long id; // Timesheet ID
    private List<TimesheetRowDTO> rows = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TimesheetRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<TimesheetRowDTO> rows) {
        this.rows = rows;
    }

    public static class TimesheetRowDTO {
        private Long projectId;
        private String category;
        private String taskKey;
        private String taskDetails;

        // Hours for each day (0=Mon, 6=Sun)
        private Double mon;
        private Double tue;
        private Double wed;
        private Double thu;
        private Double fri;
        private Double sat;
        private Double sun;

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getTaskKey() {
            return taskKey;
        }

        public void setTaskKey(String taskKey) {
            this.taskKey = taskKey;
        }

        public String getTaskDetails() {
            return taskDetails;
        }

        public void setTaskDetails(String taskDetails) {
            this.taskDetails = taskDetails;
        }

        public Double getMon() {
            return mon;
        }

        public void setMon(Double mon) {
            this.mon = mon;
        }

        public Double getTue() {
            return tue;
        }

        public void setTue(Double tue) {
            this.tue = tue;
        }

        public Double getWed() {
            return wed;
        }

        public void setWed(Double wed) {
            this.wed = wed;
        }

        public Double getThu() {
            return thu;
        }

        public void setThu(Double thu) {
            this.thu = thu;
        }

        public Double getFri() {
            return fri;
        }

        public void setFri(Double fri) {
            this.fri = fri;
        }

        public Double getSat() {
            return sat;
        }

        public void setSat(Double sat) {
            this.sat = sat;
        }

        public Double getSun() {
            return sun;
        }

        public void setSun(Double sun) {
            this.sun = sun;
        }
    }
}
