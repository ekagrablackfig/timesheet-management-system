package com.example.timesheet.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "timesheet_entries", indexes = {
        @Index(name = "idx_entry_timesheet", columnList = "timesheet_id"),
        @Index(name = "idx_entry_project", columnList = "project_id")
})
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double hours;

    // JSON string storing values e.g. {"Client": "Acme", "TaskType": "Dev"}
    @Column(columnDefinition = "TEXT")
    private String attributes;

    public TimesheetEntry() {
    }

    public TimesheetEntry(Long id, Timesheet timesheet, Project project, LocalDate date, Double hours,
            String attributes) {
        this.id = id;
        this.timesheet = timesheet;
        this.project = project;
        this.date = date;
        this.hours = hours;
        this.attributes = attributes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timesheet getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
}
