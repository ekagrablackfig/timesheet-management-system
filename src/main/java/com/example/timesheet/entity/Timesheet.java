package com.example.timesheet.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "timesheets", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "periodStartDate" }) }, indexes = {
                @Index(name = "idx_timesheet_user", columnList = "user_id"),
                @Index(name = "idx_timesheet_approver", columnList = "approver_id"),
                @Index(name = "idx_timesheet_status", columnList = "status")
        })
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate periodStartDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_approver_id")
    private User actualApprover; // Who actually performed the approval (e.g. delegate)

    @Column
    private java.time.LocalDateTime submissionDate;

    @Column
    private java.time.LocalDateTime approvalDate;

    @Column
    private String rejectionReason;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    public Timesheet() {
    }

    public Timesheet(Long id, User user, LocalDate periodStartDate, TimesheetStatus status, User approver,
            List<TimesheetEntry> entries) {
        this.id = id;
        this.user = user;
        this.periodStartDate = periodStartDate;
        this.status = status;
        this.approver = approver;
        this.entries = entries;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public List<TimesheetEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntry> entries) {
        this.entries = entries;
    }

    public User getActualApprover() {
        return actualApprover;
    }

    public void setActualApprover(User actualApprover) {
        this.actualApprover = actualApprover;
    }

    public java.time.LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(java.time.LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public java.time.LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(java.time.LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
